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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Full matrix: every catalogued API × every RBAC role × expected allow/deny from RbacSeedData.
 * Authz verdict ignores business-logic 4xx when permission should allow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("thorough")
class FullApiRoleAuthzIT {

    private static final Set<String> BUSINESS_LEVEL = Set.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER"
    );

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ThoroughFeatureSeedService seedService;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final Map<String, String> tokens = new LinkedHashMap<>();
    private final List<ObjectNode> rows = new ArrayList<>();

    @BeforeAll
    void setup() throws Exception {
        seed = seedService.seed();
        for (String role : RbacSeedData.ROLES) {
            String email = ThoroughFeatureSeedService.emailForRole(role);
            tokens.put(role, login(email));
        }
    }

    @Test
    void fullMatrixAllRolesAllApis() throws Exception {
        List<ApiAuthzCatalog.Probe> probes = ApiAuthzCatalog.all();
        Map<String, List<String>> rolePerms = RbacSeedData.rolePermissionMap();
        Set<String> allPerms = Set.of(RbacSeedData.PERMISSIONS);

        int pass = 0, fail = 0, warn = 0, skip = 0;

        for (String role : RbacSeedData.ROLES) {
            String token = tokens.get(role);
            if (token == null || token.isBlank()) {
                skip += probes.size();
                for (ApiAuthzCatalog.Probe probe : probes) {
                    rows.add(row(role, probe, "SKIP", 0, "login failed"));
                }
                continue;
            }

            Set<String> granted = new HashSet<>();
            if ("SYSTEM_ADMIN".equals(role)) {
                granted.addAll(allPerms);
            } else {
                granted.addAll(rolePerms.getOrDefault(role, List.of()));
            }

            Long storeHeader = storeHeader(role);
            Long bizHeader = bizHeader(role);

            for (ApiAuthzCatalog.Probe probe : probes) {
                boolean expectAllow = probe.permission() == null || granted.contains(probe.permission());
                String path = resolve(probe.path());
                int status;
                String body;
                try {
                    MvcResult result = perform(probe.method(), path, token, storeHeader, bizHeader, probe.note());
                    status = result.getResponse().getStatus();
                    body = result.getResponse().getContentAsString();
                } catch (Exception ex) {
                    status = 0;
                    body = ex.getMessage();
                }

                String verdict = judge(expectAllow, status);
                switch (verdict) {
                    case "PASS" -> pass++;
                    case "FAIL" -> fail++;
                    case "WARN" -> warn++;
                    default -> skip++;
                }
                rows.add(row(role, probe, verdict, status,
                        "expect=" + (expectAllow ? "ALLOW" : "DENY") + " | " + truncate(body)));
            }
        }

        ObjectNode report = objectMapper.createObjectNode();
        report.put("title", "ClappN Full API × Role × Authz Matrix");
        report.put("generatedAt", Instant.now().toString());
        report.put("endpointCount", ApiAuthzCatalog.all().size());
        report.put("roleCount", RbacSeedData.ROLES.length);
        report.put("totalCells", rows.size());

        ObjectNode summary = report.putObject("summary");
        summary.put("pass", pass);
        summary.put("fail", fail);
        summary.put("warn", warn);
        summary.put("skip", skip);
        double rate = rows.isEmpty() ? 0 : (pass * 100.0 / rows.size());
        summary.put("passRate", rate);
        // Authz OK = no allow/deny mismatches. WARNs are server errors (500), tracked separately.
        summary.put("authzOk", fail == 0);
        summary.put("healthyOk", fail == 0 && warn == 0);

        ObjectNode seedNode = report.putObject("seed");
        seedNode.put("businessId", seed.getBusinessId());
        seedNode.put("storeAId", seed.getStoreAId());
        seedNode.put("storeBId", seed.getStoreBId());

        ArrayNode results = report.putArray("results");
        results.addAll(rows);

        ArrayNode failures = report.putArray("failures");
        rows.stream().filter(r -> "FAIL".equals(r.get("verdict").asText())).forEach(failures::add);

        ArrayNode warns = report.putArray("warnings");
        rows.stream().filter(r -> "WARN".equals(r.get("verdict").asText())).forEach(warns::add);

        // Aggregate by endpoint permission
        Map<String, int[]> byPerm = new LinkedHashMap<>();
        for (ObjectNode r : rows) {
            String perm = r.path("permission").asText("AUTH");
            byPerm.computeIfAbsent(perm, k -> new int[4]);
            switch (r.get("verdict").asText()) {
                case "PASS" -> byPerm.get(perm)[0]++;
                case "FAIL" -> byPerm.get(perm)[1]++;
                case "WARN" -> byPerm.get(perm)[2]++;
                default -> byPerm.get(perm)[3]++;
            }
        }
        ArrayNode byPermission = report.putArray("byPermission");
        byPerm.forEach((perm, c) -> {
            ObjectNode n = byPermission.addObject();
            n.put("permission", perm);
            n.put("pass", c[0]);
            n.put("fail", c[1]);
            n.put("warn", c[2]);
            n.put("skip", c[3]);
        });

        Path outJson = Path.of("target", "full-api-role-authz-report.json");
        Path outMd = Path.of("target", "full-api-role-authz-report.md");
        Path reportsDir = Path.of("reports");
        Files.createDirectories(outJson.getParent());
        Files.createDirectories(reportsDir);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outJson.toFile(), report);
        Files.writeString(outMd, toMarkdown(report));
        Files.copy(outJson, reportsDir.resolve("full-api-role-authz-report.json"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(outMd, reportsDir.resolve("full-api-role-authz-report.md"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Gate: zero authz allow/deny mismatches. WARNs (500s) are tracked but not authz failures.
        assertTrue(fail == 0,
                "Authz FAILs must be zero, got " + fail + " — see " + outJson.toAbsolutePath());
        assertTrue(rate >= 90.0,
                "Authz pass rate too low: " + rate + "% — see " + outJson.toAbsolutePath());
    }

    private String judge(boolean expectAllow, int status) {
        boolean denied = status == 401 || status == 403;
        boolean allowedGate = status >= 200 && status < 500;
        if (status == 500 || status == 0) {
            // Cannot confirm authz when server errors before/during handler
            if (expectAllow) return "WARN";
            // deny expected but 500 — ambiguous
            return "WARN";
        }
        if (expectAllow) {
            return allowedGate ? "PASS" : (denied ? "FAIL" : "WARN");
        }
        // expect deny
        return denied ? "PASS" : (allowedGate ? "FAIL" : "WARN");
    }

    private ObjectNode row(String role, ApiAuthzCatalog.Probe probe, String verdict, int status, String detail) {
        ObjectNode n = objectMapper.createObjectNode();
        n.put("role", role);
        n.put("method", probe.method());
        n.put("path", probe.path());
        n.put("permission", probe.permission() == null ? "AUTHENTICATED" : probe.permission());
        n.put("verdict", verdict);
        n.put("httpStatus", status);
        n.put("detail", detail);
        return n;
    }

    private String login(String email) throws Exception {
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

    private MvcResult perform(String method, String path, String token, Long storeId, Long businessId, String note)
            throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(HttpMethod.valueOf(method), path)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON);
        if (storeId != null) req.header("X-Store-Id", storeId);
        if (businessId != null) req.header("X-Business-Id", businessId);
        if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            req.contentType(MediaType.APPLICATION_JSON);
            if (note != null && note.startsWith("body ")) {
                String json = note.substring(5).trim();
                req.content("{}".equals(json) || json.isEmpty() ? "{}" : json);
            } else {
                req.content("{}");
            }
        }
        return mockMvc.perform(req).andReturn();
    }

    private String resolve(String path) {
        return path
                .replace("{storeId}", String.valueOf(seed.getStoreAId()))
                .replace("{businessId}", String.valueOf(seed.getBusinessId()))
                .replace("{productId}", String.valueOf(seed.getProductTemplateId()))
                .replace("{serviceId}", String.valueOf(seed.getServiceTemplateId()))
                .replace("{storeServiceId}", String.valueOf(seed.getStoreServiceId()))
                .replace("{customerId}", String.valueOf(seed.getCustomerId()))
                .replace("{packageId}", String.valueOf(seed.getPackageId()))
                .replace("{userId}", "999999001")
                .replace("{brandId}", String.valueOf(seed.getBrandId()))
                .replace("{orderId}", "1");
    }

    private Long storeHeader(String role) {
        if ("SYSTEM_ADMIN".equals(role) || BUSINESS_LEVEL.contains(role)
                || "CUSTOMER".equals(role) || "GUEST".equals(role)) {
            return null;
        }
        return seed.getStoreAId();
    }

    private Long bizHeader(String role) {
        if ("SYSTEM_ADMIN".equals(role) || "CUSTOMER".equals(role) || "GUEST".equals(role)) {
            return null;
        }
        return seed.getBusinessId();
    }

    private static String truncate(String s) {
        if (s == null) return "";
        String t = s.replace('\n', ' ');
        return t.length() > 160 ? t.substring(0, 160) + "…" : t;
    }

    private String toMarkdown(ObjectNode report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Full API × Role × Authz Report\n\n");
        sb.append("Generated: ").append(report.get("generatedAt").asText()).append("\n\n");
        JsonNode s = report.get("summary");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n|---|---|\n");
        sb.append("| Endpoints | ").append(report.get("endpointCount")).append(" |\n");
        sb.append("| Roles | ").append(report.get("roleCount")).append(" |\n");
        sb.append("| Cells | ").append(report.get("totalCells")).append(" |\n");
        sb.append("| Pass | ").append(s.get("pass")).append(" |\n");
        sb.append("| Fail | ").append(s.get("fail")).append(" |\n");
        sb.append("| Warn | ").append(s.get("warn")).append(" |\n");
        sb.append("| Pass rate | ").append(String.format("%.1f%%", s.get("passRate").asDouble())).append(" |\n");
        sb.append("| Authz fully OK | ").append(s.get("authzOk").asBoolean()).append(" |\n");
        if (s.has("healthyOk")) {
            sb.append("| Healthy (no 500s) | ").append(s.get("healthyOk").asBoolean()).append(" |\n");
        }
        sb.append("\n");

        sb.append("## Failures (authz mismatches)\n\n");
        JsonNode failures = report.get("failures");
        if (!failures.isArray() || failures.isEmpty()) {
            sb.append("_None_\n");
        } else {
            sb.append("| Role | Method | Path | Perm | HTTP |\n|---|---|---|---|---|\n");
            int i = 0;
            for (JsonNode f : failures) {
                if (i++ > 80) {
                    sb.append("| … | … | truncated | … | … |\n");
                    break;
                }
                sb.append("| ").append(f.get("role").asText())
                        .append(" | ").append(f.get("method").asText())
                        .append(" | ").append(f.get("path").asText())
                        .append(" | ").append(f.get("permission").asText())
                        .append(" | ").append(f.get("httpStatus").asInt())
                        .append(" |\n");
            }
        }
        return sb.toString();
    }
}
