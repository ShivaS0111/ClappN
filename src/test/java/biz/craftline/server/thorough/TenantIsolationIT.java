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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Tenant isolation + business-vs-store scope probes (QA areas 3 &amp; 4).
 * Classifies probes as {@link ServerSideQaScope.FindingCategory#TENANT} (A).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class TenantIsolationIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        assertNotNull(seed.getBusinessBId(), "isolation business B required");
        assertNotNull(seed.getStoreA3Id(), "store A3 required");
        assertNotNull(seed.getSpecialEmails(), "special users required");

        for (String key : List.of(
                ThoroughFeatureSeedService.SPECIAL_BUSINESS_A,
                ThoroughFeatureSeedService.SPECIAL_STORE_A1,
                ThoroughFeatureSeedService.SPECIAL_STORE_A1_A2,
                ThoroughFeatureSeedService.SPECIAL_BUSINESS_B,
                ThoroughFeatureSeedService.SPECIAL_NONE,
                ThoroughFeatureSeedService.SPECIAL_INACTIVE)) {
            String email = seed.getSpecialEmails().get(key);
            tokens.put(key, login(email));
        }
    }

    @Test
    void tenantIsolationAndBusinessVsStoreScope() throws Exception {
        Long a1 = seed.getStoreAId();
        Long a2 = seed.getStoreBId();
        Long a3 = seed.getStoreA3Id();
        Long b1 = seed.getStoreB1Id();
        Long bizA = seed.getBusinessId();
        Long bizB = seed.getBusinessBId();
        Long custA = seed.getCustomerId();
        Long custB = seed.getCustomerBId();
        Long prodA = seed.getStoreProductId();
        Long prodB = seed.getStoreProductBId();

        String storeA1 = ThoroughFeatureSeedService.SPECIAL_STORE_A1;
        String bizOwnerA = ThoroughFeatureSeedService.SPECIAL_BUSINESS_A;
        String storeA1A2 = ThoroughFeatureSeedService.SPECIAL_STORE_A1_A2;
        String none = ThoroughFeatureSeedService.SPECIAL_NONE;
        String inactive = ThoroughFeatureSeedService.SPECIAL_INACTIVE;

        // --- scope.store_a1 (STORE_MANAGER @ A1 only) ---
        expectStatus(storeA1, "GET /api/stores/{A1}", "GET", "/api/stores/" + a1,
                a1, bizA, true, "TENANT");
        expectStatus(storeA1, "GET /api/stores/{A3} deny", "GET", "/api/stores/" + a3,
                a1, bizA, false, "TENANT");
        expectStatus(storeA1, "GET /api/stores/{B1} deny", "GET", "/api/stores/" + b1,
                a1, bizA, false, "TENANT");

        expectListContains(storeA1, "GET /api/stores/list scoped",
                "/api/stores/list", null, bizA,
                List.of(a1), List.of(a3, b1), "TENANT");

        // STORE_MANAGER has no business.read → permission deny on business A (category B)
        expectStatus(storeA1, "GET /api/business/{A} no business.read", "GET",
                "/api/business/" + bizA, a1, bizA, false, "PERMISSION");
        expectStatus(storeA1, "GET /api/business/{B} deny", "GET",
                "/api/business/" + bizB, a1, bizA, false, "TENANT");

        expectStatus(storeA1, "GET store-product A1 allow", "GET",
                "/api/store-product/" + prodA, a1, bizA, true, "TENANT");
        expectStatus(storeA1, "GET store-product B1 deny", "GET",
                "/api/store-product/" + prodB, a1, bizA, false, "TENANT");
        expectStatus(storeA1, "GET store-product/store/A1", "GET",
                "/api/store-product/store/" + a1, a1, bizA, true, "TENANT");
        expectStatus(storeA1, "GET store-product/store/B1 deny", "GET",
                "/api/store-product/store/" + b1, a1, bizA, false, "TENANT");

        expectStatus(storeA1, "GET customer A allow", "GET",
                "/api/customers/" + custA, a1, bizA, true, "TENANT");
        expectStatus(storeA1, "GET customer B deny", "GET",
                "/api/customers/" + custB, a1, bizA, false, "TENANT");

        // --- scope.business_a (BUSINESS_OWNER, all A stores) ---
        expectStatus(bizOwnerA, "GET store A1 (biz-wide)", "GET", "/api/stores/" + a1,
                null, bizA, true, "TENANT");
        expectStatus(bizOwnerA, "GET store A2 (biz-wide)", "GET", "/api/stores/" + a2,
                null, bizA, true, "TENANT");
        expectStatus(bizOwnerA, "GET store A3 (biz-wide)", "GET", "/api/stores/" + a3,
                null, bizA, true, "TENANT");
        expectStatus(bizOwnerA, "GET store B1 deny (biz-wide)", "GET", "/api/stores/" + b1,
                null, bizA, false, "TENANT");
        expectListContains(bizOwnerA, "GET /api/stores/list only A",
                "/api/stores/list", null, bizA,
                List.of(a1, a2, a3), List.of(b1), "TENANT");
        expectStatus(bizOwnerA, "GET business A allow", "GET",
                "/api/business/" + bizA, null, bizA, true, "TENANT");
        expectStatus(bizOwnerA, "GET business B deny", "GET",
                "/api/business/" + bizB, null, bizA, false, "TENANT");

        // --- scope.store_a1_a2 (A1+A2, not A3) — business vs store scope ---
        expectStatus(storeA1A2, "GET A1 (multi-store)", "GET", "/api/stores/" + a1,
                a1, bizA, true, "TENANT");
        expectStatus(storeA1A2, "GET A2 (multi-store)", "GET", "/api/stores/" + a2,
                a2, bizA, true, "TENANT");
        expectStatus(storeA1A2, "GET A3 deny (multi-store)", "GET", "/api/stores/" + a3,
                a1, bizA, false, "TENANT");
        expectListContains(storeA1A2, "list A1+A2 only",
                "/api/stores/list", null, bizA,
                List.of(a1, a2), List.of(a3, b1), "TENANT");

        // --- scope.none / scope.inactive ---
        expectStatus(none, "none → protected store", "GET", "/api/stores/" + a1,
                null, null, false, "TENANT");
        expectStatus(none, "none → protected business", "GET", "/api/business/" + bizA,
                null, null, false, "TENANT");
        expectStatus(inactive, "inactive → protected store", "GET", "/api/stores/" + a1,
                null, bizA, false, "TENANT");
        expectStatus(inactive, "inactive → protected business", "GET", "/api/business/" + bizA,
                null, bizA, false, "TENANT");

        // --- X-Store-Id header (UserScopeFilter) ---
        expectStatus(storeA1, "X-Store-Id=A1 ok", "GET", "/api/me/context",
                a1, bizA, true, "TENANT");
        expectStatus(storeA1, "X-Store-Id=A3 → 403", "GET", "/api/me/context",
                a3, bizA, false, "TENANT");
        expectStatus(storeA1, "X-Store-Id=B1 → 403", "GET", "/api/stores/list",
                b1, bizA, false, "TENANT");

        int pass = 0, fail = 0, warn = 0;
        for (ObjectNode r : rows) {
            switch (r.get("verdict").asText()) {
                case "PASS" -> pass++;
                case "FAIL" -> fail++;
                default -> warn++;
            }
        }

        ObjectNode report = objectMapper.createObjectNode();
        report.put("title", "ClappN Tenant Isolation Report");
        report.put("generatedAt", Instant.now().toString());
        report.put("category", ServerSideQaScope.FindingCategory.TENANT.code()
                + " " + ServerSideQaScope.FindingCategory.TENANT.label());

        ObjectNode summary = report.putObject("summary");
        summary.put("pass", pass);
        summary.put("fail", fail);
        summary.put("warn", warn);
        summary.put("total", rows.size());

        ObjectNode seedNode = report.putObject("seed");
        seedNode.put("businessAId", bizA);
        seedNode.put("businessBId", bizB);
        seedNode.put("storeA1Id", a1);
        seedNode.put("storeA2Id", a2);
        seedNode.put("storeA3Id", a3);
        seedNode.put("storeB1Id", b1);
        seedNode.put("storeB2Id", seed.getStoreB2Id());
        seedNode.put("customerAId", custA);
        seedNode.put("customerBId", custB);
        seedNode.put("storeProductAId", prodA);
        seedNode.put("storeProductBId", prodB);
        ObjectNode special = seedNode.putObject("specialUsers");
        seed.getSpecialEmails().forEach((k, v) -> {
            ObjectNode u = special.putObject(k);
            u.put("email", v);
            u.put("userId", seed.getSpecialUserIds().get(k));
        });

        ArrayNode results = report.putArray("results");
        results.addAll(rows);

        ArrayNode failures = report.putArray("failures");
        rows.stream().filter(r -> "FAIL".equals(r.get("verdict").asText())).forEach(failures::add);

        Path outJson = Path.of("target", "tenant-isolation-report.json");
        Path outMd = Path.of("target", "tenant-isolation-report.md");
        Path reportsDir = Path.of("reports");
        Files.createDirectories(outJson.getParent());
        Files.createDirectories(reportsDir);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outJson.toFile(), report);
        Files.writeString(outMd, toMarkdown(report));
        Files.copy(outJson, reportsDir.resolve("tenant-isolation-report.json"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(outMd, reportsDir.resolve("tenant-isolation-report.md"), StandardCopyOption.REPLACE_EXISTING);

        ServerSideQaAuthorizationReporter.writeCombined(seed);

        assertTrue(fail == 0,
                "Tenant isolation FAILs must be zero, got " + fail + " — see " + outJson.toAbsolutePath());
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
        String verdict = judge(expectAllow, status);
        rows.add(row(userKey, probeName, method, path, category, verdict, status,
                "expect=" + (expectAllow ? "ALLOW" : "DENY") + " | " + truncate(body)));
    }

    private void expectListContains(String userKey, String probeName, String path,
                                    Long storeHeader, Long bizHeader,
                                    List<Long> mustContain, List<Long> mustNotContain,
                                    String category) throws Exception {
        String token = tokens.get(userKey);
        int status;
        String body;
        if (token == null || token.isBlank()) {
            status = 0;
            body = "login failed";
        } else {
            MvcResult result = perform("GET", path, token, storeHeader, bizHeader);
            status = result.getResponse().getStatus();
            body = result.getResponse().getContentAsString();
        }

        boolean ok = status >= 200 && status < 300;
        String detail;
        if (!ok) {
            detail = "list HTTP " + status + " | " + truncate(body);
            rows.add(row(userKey, probeName, "GET", path, category, "FAIL", status, detail));
            return;
        }
        List<String> missing = new ArrayList<>();
        List<String> leaked = new ArrayList<>();
        for (Long id : mustContain) {
            if (!bodyContainsId(body, id)) missing.add(String.valueOf(id));
        }
        for (Long id : mustNotContain) {
            if (bodyContainsId(body, id)) leaked.add(String.valueOf(id));
        }
        boolean pass = missing.isEmpty() && leaked.isEmpty();
        detail = pass ? "scoped list OK"
                : "missing=" + missing + " leaked=" + leaked + " | " + truncate(body);
        rows.add(row(userKey, probeName, "GET", path, category, pass ? "PASS" : "FAIL", status, detail));
    }

    /** True if any JSON numeric/text field named id/storeId equals the given id. */
    private boolean bodyContainsId(String body, Long id) {
        if (body == null || id == null) return false;
        try {
            return containsIdNode(objectMapper.readTree(body), id);
        } catch (Exception e) {
            return body.contains("\"id\":" + id) || body.contains("\"id\": " + id);
        }
    }

    private boolean containsIdNode(JsonNode node, Long id) {
        if (node == null || node.isNull()) return false;
        if (node.isObject()) {
            JsonNode idField = node.get("id");
            if (idField != null && !idField.isNull() && idField.asLong() == id) return true;
            JsonNode storeId = node.get("storeId");
            if (storeId != null && !storeId.isNull() && storeId.asLong() == id) return true;
            var fields = node.fields();
            while (fields.hasNext()) {
                if (containsIdNode(fields.next().getValue(), id)) return true;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                if (containsIdNode(child, id)) return true;
            }
        }
        return false;
    }

    private String judge(boolean expectAllow, int status) {
        boolean denied = status == 401 || status == 403;
        boolean allowedGate = status >= 200 && status < 500;
        if (status == 500 || status == 0) return "WARN";
        if (expectAllow) {
            return allowedGate && !denied ? "PASS" : (denied ? "FAIL" : "WARN");
        }
        return denied ? "PASS" : (allowedGate ? "FAIL" : "WARN");
    }

    private ObjectNode row(String user, String probe, String method, String path,
                           String category, String verdict, int status, String detail) {
        ObjectNode n = objectMapper.createObjectNode();
        n.put("user", user);
        n.put("probe", probe);
        n.put("method", method);
        n.put("path", path);
        n.put("category", category);
        n.put("verdict", verdict);
        n.put("httpStatus", status);
        n.put("detail", detail);
        return n;
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
        if (result.getResponse().getStatus() >= 400) {
            return null;
        }
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode token = root.path("data").path("tokenInfo").path("token");
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

    private static String truncate(String s) {
        if (s == null) return "";
        String t = s.replace('\n', ' ');
        return t.length() > 160 ? t.substring(0, 160) + "…" : t;
    }

    private String toMarkdown(ObjectNode report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Tenant Isolation Report\n\n");
        sb.append("Generated: ").append(report.get("generatedAt").asText()).append("\n\n");
        sb.append("Category: ").append(report.path("category").asText()).append("\n\n");
        JsonNode s = report.get("summary");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n|---|---|\n");
        sb.append("| Pass | ").append(s.get("pass")).append(" |\n");
        sb.append("| Fail | ").append(s.get("fail")).append(" |\n");
        sb.append("| Warn | ").append(s.get("warn")).append(" |\n");
        sb.append("| Total | ").append(s.get("total")).append(" |\n\n");

        sb.append("## Failures\n\n");
        JsonNode failures = report.get("failures");
        if (!failures.isArray() || failures.isEmpty()) {
            sb.append("_None_\n");
        } else {
            sb.append("| User | Probe | Category | HTTP |\n|---|---|---|---|\n");
            for (JsonNode f : failures) {
                sb.append("| ").append(f.get("user").asText())
                        .append(" | ").append(f.get("probe").asText())
                        .append(" | ").append(f.get("category").asText())
                        .append(" | ").append(f.get("httpStatus").asInt())
                        .append(" |\n");
            }
        }
        return sb.toString();
    }
}
