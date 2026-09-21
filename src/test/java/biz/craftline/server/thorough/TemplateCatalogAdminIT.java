package biz.craftline.server.thorough;

import biz.craftline.server.config.RbacSeedData;
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
 * Category C templates/catalogs + admin-only APIs (QA areas 7–9).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class TemplateCatalogAdminIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        assertNotNull(seed.getSpecialEmails());

        tokens.put("SYSTEM_ADMIN", login(ThoroughFeatureSeedService.emailForRole("SYSTEM_ADMIN")));
        tokens.put("CASHIER", login(ThoroughFeatureSeedService.emailForRole("CASHIER")));
        tokens.put(ThoroughFeatureSeedService.SPECIAL_STORE_A1,
                login(seed.getSpecialEmails().get(ThoroughFeatureSeedService.SPECIAL_STORE_A1)));
    }

    @Test
    void templatesCatalogsAndAdminApis() throws Exception {
        String storeA1 = ThoroughFeatureSeedService.SPECIAL_STORE_A1;
        Long a1 = seed.getStoreAId();
        Long bizA = seed.getBusinessId();

        List<String> storeManagerPerms = RbacSeedData.rolePermissionMap().getOrDefault("STORE_MANAGER", List.of());
        List<String> cashierPerms = RbacSeedData.rolePermissionMap().getOrDefault("CASHIER", List.of());
        boolean storeMgrHasBrandRead = storeManagerPerms.contains("brand.read");
        boolean cashierHasBrandCreate = cashierPerms.contains("brand.create");

        // --- Global catalogs ---
        expectStatus("SYSTEM_ADMIN", "SYSTEM_ADMIN GET /api/brands", "GET", "/api/brands",
                null, null, true, "SHARED_CATALOG");
        expectStatus("SYSTEM_ADMIN", "SYSTEM_ADMIN GET /api/categories/list", "GET",
                "/api/categories/list", null, null, true, "SHARED_CATALOG");
        expectStatus("SYSTEM_ADMIN", "SYSTEM_ADMIN GET /api/business-type/list", "GET",
                "/api/business-type/list", null, null, true, "SHARED_CATALOG");

        expectStatus(storeA1, "store_a1 GET /api/brands (brand.read=" + storeMgrHasBrandRead + ")",
                "GET", "/api/brands", a1, bizA, storeMgrHasBrandRead, "SHARED_CATALOG");

        ObjectNode brandBody = objectMapper.createObjectNode();
        brandBody.put("name", "Thorough Unauthorized Brand");
        MvcResult createBrand = performBody("POST", "/api/brands", tokens.get("CASHIER"),
                null, null, brandBody.toString());
        int createStatus = createBrand.getResponse().getStatus();
        String createVerdict;
        if (!cashierHasBrandCreate) {
            createVerdict = (createStatus == 401 || createStatus == 403) ? "PASS" : "FAIL";
        } else {
            createVerdict = ThoroughSliceReportHelper.judge(true, createStatus);
        }
        rows.add(row("CASHIER", "CASHIER POST /api/brands (brand.create=" + cashierHasBrandCreate + ")",
                "POST", "/api/brands", "PERMISSION", createVerdict, createStatus,
                ThoroughSliceReportHelper.truncate(createBrand.getResponse().getContentAsString())));

        // --- Templates (business-type scoped shared catalog) ---
        expectStatus(storeA1, "GET /api/business-product/list (product.read)", "GET",
                "/api/business-product/list", a1, bizA, true, "SHARED_CATALOG");

        // --- Admin-only (service-layer SYSTEM_ADMIN gate) ---
        expectStatus("SYSTEM_ADMIN", "SYSTEM_ADMIN GET /api/delivery-info", "GET",
                "/api/delivery-info", null, null, true, "ADMIN");
        expectStatus(storeA1, "store_a1 GET /api/delivery-info → 403", "GET",
                "/api/delivery-info", a1, bizA, false, "ADMIN");

        expectStatus(storeA1, "store_a1 GET /api/virtual-product-details → 403", "GET",
                "/api/virtual-product-details", a1, bizA, false, "ADMIN");
        expectStatus("SYSTEM_ADMIN", "SYSTEM_ADMIN GET /api/virtual-product-details", "GET",
                "/api/virtual-product-details", null, null, true, "ADMIN");

        ThoroughSliceReportHelper.Counts counts = ThoroughSliceReportHelper.count(rows);
        ObjectNode report = ThoroughSliceReportHelper.baseReport(objectMapper,
                "ClappN Template / Catalog / Admin Report",
                ServerSideQaScope.FindingCategory.SHARED_CATALOG.code() + " "
                        + ServerSideQaScope.FindingCategory.SHARED_CATALOG.label(),
                counts);
        ArrayNode notes = report.putArray("notes");
        notes.add("SHARED_CATALOG: templates are business-type scoped, not business-id scoped");
        notes.add("STORE_MANAGER brand.read=" + storeMgrHasBrandRead
                + " → " + (storeMgrHasBrandRead ? "ALLOW on /api/brands is correct" : "403 expected"));
        notes.add("Delivery-info and virtual-product-details list endpoints require SYSTEM_ADMIN "
                + "(service-layer AccessDeniedException), not merely order.read/product.read");
        ThoroughSliceReportHelper.writeFiles(objectMapper, "template-catalog-admin-report", report, rows);
        ServerSideQaAuthorizationReporter.writeCombined(seed);

        assertTrue(counts.fail() == 0,
                "TemplateCatalogAdminIT FAILs must be zero, got " + counts.fail());
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
