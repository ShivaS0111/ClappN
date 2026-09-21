package biz.craftline.server.thorough;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * CRUD + IDOR + products + mutation integrity (QA areas 5/6/11/13).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class CrudIdorProductsIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        assertNotNull(seed.getBusinessBId());
        assertNotNull(seed.getSpecialEmails());

        for (String key : List.of(
                ThoroughFeatureSeedService.SPECIAL_STORE_A1,
                ThoroughFeatureSeedService.SPECIAL_BUSINESS_A,
                ThoroughFeatureSeedService.SPECIAL_BUSINESS_B)) {
            tokens.put(key, login(seed.getSpecialEmails().get(key)));
        }
        tokens.put("CASHIER", login(ThoroughFeatureSeedService.emailForRole("CASHIER")));
    }

    @Test
    void crudIdorProductsAndMutationIntegrity() throws Exception {
        Long a1 = seed.getStoreAId();
        Long a3 = seed.getStoreA3Id();
        Long b1 = seed.getStoreB1Id();
        Long bizA = seed.getBusinessId();
        Long bizB = seed.getBusinessBId();
        Long custA = seed.getCustomerId();
        Long custB = seed.getCustomerBId();

        String storeA1 = ThoroughFeatureSeedService.SPECIAL_STORE_A1;
        String bizAOwner = ThoroughFeatureSeedService.SPECIAL_BUSINESS_A;
        String bizBOwner = ThoroughFeatureSeedService.SPECIAL_BUSINESS_B;

        // --- IDOR / get ---
        expectStatus(storeA1, "GET store A1 allow", "GET", "/api/stores/" + a1,
                a1, bizA, true, "IDOR");
        expectStatus(storeA1, "GET store A3 deny", "GET", "/api/stores/" + a3,
                a1, bizA, false, "IDOR");
        expectStatus(storeA1, "GET store B1 deny", "GET", "/api/stores/" + b1,
                a1, bizA, false, "IDOR");

        expectStatus(storeA1, "GET customer A allow", "GET", "/api/customers/" + custA,
                a1, bizA, true, "IDOR");
        expectStatus(storeA1, "GET customer B deny", "GET", "/api/customers/" + custB,
                a1, bizA, false, "IDOR");

        // Capture A3 name before foreign mutation attempt
        String a3NameBefore = fetchStoreName(bizAOwner, a3, null, bizA);

        // PUT foreign store A3 → 403 + unchanged
        ObjectNode putA3 = objectMapper.createObjectNode();
        putA3.put("storeName", "HACKED_A3");
        MvcResult putA3Result = performBody("PUT", "/api/stores/" + a3, tokens.get(storeA1),
                a1, bizA, putA3.toString());
        int putA3Status = putA3Result.getResponse().getStatus();
        boolean putA3Denied = putA3Status == 401 || putA3Status == 403;
        rows.add(row(storeA1, "PUT foreign store A3 → 403", "PUT", "/api/stores/" + a3,
                "IDOR", putA3Denied ? "PASS" : "FAIL", putA3Status,
                ThoroughSliceReportHelper.truncate(putA3Result.getResponse().getContentAsString())));

        String a3NameAfter = fetchStoreName(bizAOwner, a3, null, bizA);
        boolean a3Unchanged = a3NameBefore != null && a3NameBefore.equals(a3NameAfter)
                && !"HACKED_A3".equals(a3NameAfter);
        rows.add(row(bizAOwner, "A3 name unchanged after foreign PUT", "GET", "/api/stores/" + a3,
                "INTEGRITY", a3Unchanged ? "PASS" : "FAIL", 200,
                "before=" + a3NameBefore + " after=" + a3NameAfter));

        // DELETE foreign store B1 as store_a1 → 403 + still GET-able as business_b
        MvcResult delB1 = perform("DELETE", "/api/stores/" + b1, tokens.get(storeA1), a1, bizA);
        int delB1Status = delB1.getResponse().getStatus();
        boolean delDenied = delB1Status == 401 || delB1Status == 403;
        rows.add(row(storeA1, "DELETE foreign store B1 → 403", "DELETE", "/api/stores/" + b1,
                "IDOR", delDenied ? "PASS" : "FAIL", delB1Status,
                ThoroughSliceReportHelper.truncate(delB1.getResponse().getContentAsString())));

        expectStatus(bizBOwner, "B1 still readable after denied DELETE", "GET", "/api/stores/" + b1,
                null, bizB, true, "INTEGRITY");

        // --- CRUD permission ---
        MvcResult cashierDel = perform("DELETE", "/api/stores/" + a1, tokens.get("CASHIER"), a1, bizA);
        int cashierDelStatus = cashierDel.getResponse().getStatus();
        rows.add(row("CASHIER", "CASHIER DELETE A1 → 403 (no store.delete)", "DELETE",
                "/api/stores/" + a1, "PERMISSION",
                (cashierDelStatus == 401 || cashierDelStatus == 403) ? "PASS" : "FAIL",
                cashierDelStatus,
                ThoroughSliceReportHelper.truncate(cashierDel.getResponse().getContentAsString())));

        expectStatus(bizAOwner, "BUSINESS_OWNER list stores A", "GET", "/api/stores/list",
                null, bizA, true, "CRUD");

        // --- Products ---
        expectStatus(storeA1, "GET store-product/store/A1 allow", "GET",
                "/api/store-product/store/" + a1, a1, bizA, true, "PRODUCT");
        expectStatus(storeA1, "GET store-product/store/B1 deny", "GET",
                "/api/store-product/store/" + b1, a1, bizA, false, "PRODUCT");

        MvcResult emptyCreate = performBody("POST", "/api/store-product/save", tokens.get(storeA1),
                a1, bizA, "{}");
        int emptyStatus = emptyCreate.getResponse().getStatus();
        rows.add(row(storeA1, "POST empty store-product → 4xx not 500", "POST",
                "/api/store-product/save", "PRODUCT",
                ThoroughSliceReportHelper.judgeClientError(emptyStatus), emptyStatus,
                ThoroughSliceReportHelper.truncate(emptyCreate.getResponse().getContentAsString())));

        // --- Mutation no-op: PUT B1 as store_a1 ---
        String b1NameBefore = fetchStoreName(bizBOwner, b1, null, bizB);
        ObjectNode hack = objectMapper.createObjectNode();
        hack.put("storeName", "HACKED");
        MvcResult putB1 = performBody("PUT", "/api/stores/" + b1, tokens.get(storeA1),
                a1, bizA, hack.toString());
        int putB1Status = putB1.getResponse().getStatus();
        rows.add(row(storeA1, "PUT B1 as store_a1 → 403", "PUT", "/api/stores/" + b1,
                "INTEGRITY",
                (putB1Status == 401 || putB1Status == 403) ? "PASS" : "FAIL", putB1Status,
                ThoroughSliceReportHelper.truncate(putB1.getResponse().getContentAsString())));

        String b1NameAfter = fetchStoreName(bizBOwner, b1, null, bizB);
        boolean notHacked = b1NameAfter != null && !"HACKED".equals(b1NameAfter)
                && (b1NameBefore == null || b1NameBefore.equals(b1NameAfter));
        rows.add(row(bizBOwner, "B1 storeName not HACKED after denied PUT", "GET",
                "/api/stores/" + b1, "INTEGRITY", notHacked ? "PASS" : "FAIL", 200,
                "before=" + b1NameBefore + " after=" + b1NameAfter));

        ThoroughSliceReportHelper.Counts counts = ThoroughSliceReportHelper.count(rows);
        ObjectNode report = ThoroughSliceReportHelper.baseReport(objectMapper,
                "ClappN CRUD / IDOR / Products Report",
                ServerSideQaScope.FindingCategory.TENANT.code() + "+"
                        + ServerSideQaScope.FindingCategory.PERMISSION.code(),
                counts);
        ThoroughSliceReportHelper.writeFiles(objectMapper, "crud-idor-report", report, rows);
        ServerSideQaAuthorizationReporter.writeCombined(seed);

        assertTrue(counts.fail() == 0,
                "CrudIdorProductsIT FAILs must be zero, got " + counts.fail());
    }

    private String fetchStoreName(String userKey, Long storeId, Long storeHeader, Long bizHeader)
            throws Exception {
        MvcResult r = perform("GET", "/api/stores/" + storeId, tokens.get(userKey), storeHeader, bizHeader);
        if (r.getResponse().getStatus() >= 400) return null;
        try {
            JsonNode data = objectMapper.readTree(r.getResponse().getContentAsString()).path("data");
            JsonNode name = data.path("storeName");
            if (name.isMissingNode() || name.isNull()) name = data.path("name");
            return name.isMissingNode() ? null : name.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private void expectStatus(String userKey, String probeName, String method, String path,
                              Long storeHeader, Long bizHeader, boolean expectAllow, String category)
            throws Exception {
        String token = tokens.get(userKey);
        int status;
        String body;
        if (token == null || token.isBlank()) {
            status = 0;
            body = "login failed for " + userKey;
        } else {
            MvcResult result = perform(method, path, token, storeHeader, bizHeader);
            status = result.getResponse().getStatus();
            body = result.getResponse().getContentAsString();
        }
        rows.add(row(userKey, probeName, method, path, category,
                ThoroughSliceReportHelper.judge(expectAllow, status), status,
                "expect=" + (expectAllow ? "ALLOW" : "DENY") + " | "
                        + ThoroughSliceReportHelper.truncate(body)));
    }

    private ObjectNode row(String user, String probe, String method, String path,
                           String category, String verdict, int status, String detail) {
        return ThoroughSliceReportHelper.row(objectMapper, user, probe, method, path,
                category, verdict, status, detail);
    }

    private String login(String email) throws Exception {
        if (email == null) return null;
        ObjectNode body = objectMapper.createObjectNode();
        body.put("username", email);
        body.put("password", ThoroughFeatureSeedService.PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andReturn();
        if (result.getResponse().getStatus() >= 400) return null;
        JsonNode token = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("tokenInfo").path("token");
        return token.isMissingNode() ? null : token.asText();
    }

    private MvcResult perform(String method, String path, String token, Long storeId, Long businessId)
            throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(HttpMethod.valueOf(method), path)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON);
        if (storeId != null) req.header("X-Store-Id", storeId);
        if (businessId != null) req.header("X-Business-Id", businessId);
        return mockMvc.perform(req).andReturn();
    }

    private MvcResult performBody(String method, String path, String token,
                                  Long storeId, Long businessId, String jsonBody) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(HttpMethod.valueOf(method), path)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonBody == null ? "{}" : jsonBody);
        if (storeId != null) req.header("X-Store-Id", storeId);
        if (businessId != null) req.header("X-Business-Id", businessId);
        return mockMvc.perform(req).andReturn();
    }
}
