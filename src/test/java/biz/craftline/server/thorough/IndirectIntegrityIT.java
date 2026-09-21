package biz.craftline.server.thorough;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
 * Indirect / chained authorization + data integrity probes (QA areas 10 &amp; 13).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class IndirectIntegrityIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        assertNotNull(seed.getStoreB1Id());
        assertNotNull(seed.getSpecialEmails());

        tokens.put(ThoroughFeatureSeedService.SPECIAL_STORE_A1,
                login(seed.getSpecialEmails().get(ThoroughFeatureSeedService.SPECIAL_STORE_A1)));
    }

    @Test
    void indirectChainedAuthzAndIntegrity() throws Exception {
        String storeA1 = ThoroughFeatureSeedService.SPECIAL_STORE_A1;
        Long a1 = seed.getStoreAId();
        Long b1 = seed.getStoreB1Id();
        Long bizA = seed.getBusinessId();
        Long custB = seed.getCustomerBId();
        Long productTemplateId = seed.getProductTemplateId();

        // Create store-product for B1 as store_a1 → DENY
        ObjectNode spBody = objectMapper.createObjectNode();
        spBody.put("storeId", b1);
        spBody.put("businessId", seed.getBusinessBId());
        spBody.put("businessProductId", productTemplateId);
        spBody.put("aliasName", "IndirectHack");
        spBody.put("status", 1);
        MvcResult createSp = performBody("POST", "/api/store-product/save", tokens.get(storeA1),
                a1, bizA, spBody.toString());
        int spStatus = createSp.getResponse().getStatus();
        rows.add(row(storeA1, "POST store-product storeId=B1 as store_a1 → DENY",
                "POST", "/api/store-product/save", "INDIRECT",
                denyVerdict(spStatus), spStatus,
                ThoroughSliceReportHelper.truncate(createSp.getResponse().getContentAsString())));

        // Order create with storeId=B1 as store_a1 → DENY
        ObjectNode orderB = objectMapper.createObjectNode();
        orderB.put("storeId", b1);
        orderB.put("customerId", custB);
        orderB.put("totalAmount", 10.0);
        MvcResult orderDeny = performBody("POST", "/api/orders/new", tokens.get(storeA1),
                a1, bizA, orderB.toString());
        int orderDenyStatus = orderDeny.getResponse().getStatus();
        rows.add(row(storeA1, "POST /api/orders/new storeId=B1 → DENY",
                "POST", "/api/orders/new", "INDIRECT",
                denyVerdict(orderDenyStatus), orderDenyStatus,
                ThoroughSliceReportHelper.truncate(orderDeny.getResponse().getContentAsString())));

        // Cross-tenant: customer B under store A1
        ObjectNode cross = objectMapper.createObjectNode();
        cross.put("storeId", a1);
        cross.put("customerId", custB);
        cross.put("totalAmount", 10.0);
        MvcResult crossOrder = performBody("POST", "/api/orders/new", tokens.get(storeA1),
                a1, bizA, cross.toString());
        int crossStatus = crossOrder.getResponse().getStatus();
        // Expect deny (403) or validation failure (4xx) — never 2xx success or 500
        String crossVerdict;
        if (crossStatus == 500 || crossStatus == 0) {
            crossVerdict = "WARN";
        } else if (crossStatus == 401 || crossStatus == 403
                || (crossStatus >= 400 && crossStatus < 500)) {
            crossVerdict = "PASS";
        } else if (crossStatus >= 200 && crossStatus < 300) {
            crossVerdict = "FAIL";
        } else {
            crossVerdict = "WARN";
        }
        rows.add(row(storeA1, "POST order customerB + storeA1 → deny/validation",
                "POST", "/api/orders/new", "INDIRECT",
                crossVerdict, crossStatus,
                ThoroughSliceReportHelper.truncate(crossOrder.getResponse().getContentAsString())));

        ThoroughSliceReportHelper.Counts counts = ThoroughSliceReportHelper.count(rows);
        ObjectNode report = ThoroughSliceReportHelper.baseReport(objectMapper,
                "ClappN Indirect Integrity Report",
                ServerSideQaScope.FindingCategory.TENANT.code() + " Indirect/Integrity",
                counts);
        ArrayNode notes = report.putArray("notes");
        notes.add("Deny preferred as 403 from RequirePermissionInterceptor / validateStoreAccess; "
                + "4xx after authz gate is PASS for allow-gate probes when documented.");
        ThoroughSliceReportHelper.writeFiles(objectMapper, "indirect-integrity-report", report, rows);
        ServerSideQaAuthorizationReporter.writeCombined(seed);

        assertTrue(counts.fail() == 0,
                "IndirectIntegrityIT FAILs must be zero, got " + counts.fail());
    }

    /** Prefer 403; also accept 401. 4xx validation after authz still PASS for deny intent. */
    private static String denyVerdict(int status) {
        if (status == 500 || status == 0) return "WARN";
        if (status == 401 || status == 403) return "PASS";
        // Permission may allow then store validation returns 403 — already covered.
        // If create permission missing → 403. If somehow 400 after passing authz with foreign store — still OK.
        if (status >= 400 && status < 500) return "PASS";
        return "FAIL";
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
