package biz.craftline.server.thorough;

import biz.craftline.server.config.RbacSeedData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Seeds thorough test users/data against MySQL and probes core APIs per role.
 * Run: mvnw -Dtest=ThoroughSystemFeatureIT -Dspring.profiles.active=thorough test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("thorough")
class ThoroughSystemFeatureIT {

    private static final Set<String> BUSINESS_LEVEL = Set.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER"
    );

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ThoroughFeatureSeedService seedService;

    @Autowired
    private ObjectMapper objectMapper;

    private ThoroughFeatureSeedService.SeedSnapshot seed;
    private final List<ProbeResult> probes = new ArrayList<>();
    private final Map<String, LoginOutcome> logins = new LinkedHashMap<>();

    @BeforeAll
    void seedAll() {
        seed = seedService.seed();
        assertNotNull(seed.getBusinessId());
        assertTrue(seed.getRoleCount() >= 30);
    }

    @Test
    @Order(1)
    void loginEveryRole() {
        for (String role : RbacSeedData.ROLES) {
            String email = ThoroughFeatureSeedService.emailForRole(role);
            LoginOutcome outcome = login(email);
            logins.put(role, outcome);
            probes.add(ProbeResult.of("AUTH", role, "POST /api/auth/login",
                    outcome.ok() ? "PASS" : "FAIL",
                    outcome.status(),
                    outcome.ok() ? "token issued" : outcome.message()));
        }
        long ok = logins.values().stream().filter(LoginOutcome::ok).count();
        assertTrue(ok >= RbacSeedData.ROLES.length - 2,
                "Expected nearly all roles to login, got " + ok);
    }

    @Test
    @Order(2)
    void meContextAndScope() {
        for (Map.Entry<String, LoginOutcome> e : logins.entrySet()) {
            if (!e.getValue().ok()) continue;
            String role = e.getKey();
            Long storeHeader = storeHeaderFor(role);
            Long bizHeader = seed.getBusinessId();
            if ("SYSTEM_ADMIN".equals(role) || "CUSTOMER".equals(role) || "GUEST".equals(role)) {
                bizHeader = null;
                storeHeader = null;
            } else if (BUSINESS_LEVEL.contains(role)) {
                storeHeader = null;
            }

            ResponseEntity<String> resp = get("/api/me/context", e.getValue().token(), storeHeader, bizHeader);
            boolean pass = resp.getStatusCode().is2xxSuccessful();
            probes.add(ProbeResult.of("SCOPE", role, "GET /api/me/context",
                    pass ? "PASS" : "FAIL",
                    resp.getStatusCode().value(),
                    truncate(resp.getBody())));
        }
    }

    @Test
    @Order(3)
    void featureReadsByRoleMatrix() {
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("business.read", "/api/business");
        endpoints.put("store.read", "/api/stores");
        endpoints.put("product.read", "/api/business-product");
        endpoints.put("service.read", "/api/business-service");
        endpoints.put("brand.read", "/api/brands");
        endpoints.put("category.read", "/api/categories/list");
        endpoints.put("order.read", "/api/orders");
        endpoints.put("user.read", "/api/employees");
        endpoints.put("customer.read", "/api/customers");
        endpoints.put("invoice.read", "/api/invoices");
        endpoints.put("package.read", "/api/store-offered-packages");
        endpoints.put("store_product.read", "/api/store-product");
        endpoints.put("store_service.read", "/api/store-service");
        endpoints.put("inventory.read", "/api/store-inventory/" + seed.getStoreAId());

        Map<String, List<String>> rolePerms = RbacSeedData.rolePermissionMap();

        // Focus roles for matrix depth + sample of others
        List<String> focus = List.of(
                "SYSTEM_ADMIN", "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER",
                "STORE_OWNER", "STORE_MANAGER", "CASHIER", "INVENTORY_STAFF",
                "FINANCE_MANAGER", "CUSTOMER", "GUEST", "SALES_ASSOCIATE"
        );

        for (String role : focus) {
            LoginOutcome login = logins.get(role);
            if (login == null || !login.ok()) {
                probes.add(ProbeResult.of("MATRIX", role, "(skip)", "SKIP", 0, "login failed"));
                continue;
            }
            Set<String> granted = "SYSTEM_ADMIN".equals(role)
                    ? Set.of(RbacSeedData.PERMISSIONS)
                    : Set.copyOf(rolePerms.getOrDefault(role, List.of()));

            Long storeHeader = storeHeaderFor(role);
            Long bizHeader = ("SYSTEM_ADMIN".equals(role) || "CUSTOMER".equals(role) || "GUEST".equals(role))
                    ? null : seed.getBusinessId();
            if (BUSINESS_LEVEL.contains(role)) storeHeader = null;

            for (Map.Entry<String, String> ep : endpoints.entrySet()) {
                String permission = ep.getKey();
                boolean expectAllow = granted.contains(permission);
                ResponseEntity<String> resp = get(ep.getValue(), login.token(), storeHeader, bizHeader);
                int code = resp.getStatusCode().value();
                boolean allowed = code >= 200 && code < 300;
                boolean denied = code == 403 || code == 401;
                String verdict;
                if (expectAllow && allowed) verdict = "PASS";
                else if (!expectAllow && denied) verdict = "PASS";
                else if (expectAllow && denied) verdict = "FAIL";
                else if (!expectAllow && allowed) verdict = "FAIL";
                else verdict = "WARN"; // e.g. 404/500

                probes.add(ProbeResult.of("MATRIX", role,
                        "GET " + ep.getValue() + " [" + permission + "] expect=" + (expectAllow ? "ALLOW" : "DENY"),
                        verdict, code, truncate(resp.getBody())));
            }
        }
    }

    @Test
    @Order(4)
    void writePathSmokeAsBusinessOwnerAndCashier() {
        LoginOutcome owner = logins.get("BUSINESS_OWNER");
        LoginOutcome cashier = logins.get("CASHIER");
        assertNotNull(owner);
        assertTrue(owner.ok());

        // Owner creates a customer
        ObjectNode customerBody = objectMapper.createObjectNode();
        customerBody.put("firstName", "Probe");
        customerBody.put("lastName", "Buyer");
        customerBody.put("email", "probe.buyer." + Instant.now().toEpochMilli() + "@clapp.test");
        customerBody.put("phone", "+15550001111");
        customerBody.put("storeId", seed.getStoreAId());
        customerBody.put("businessId", seed.getBusinessId());

        ResponseEntity<String> createCustomer = post("/api/customers", owner.token(),
                seed.getStoreAId(), seed.getBusinessId(), customerBody);
        probes.add(ProbeResult.of("WRITE", "BUSINESS_OWNER", "POST /api/customers",
                createCustomer.getStatusCode().is2xxSuccessful() ? "PASS" : "FAIL",
                createCustomer.getStatusCode().value(), truncate(createCustomer.getBody())));

        // Cashier should create order if permitted
        if (cashier != null && cashier.ok()) {
            ObjectNode order = objectMapper.createObjectNode();
            order.put("storeId", seed.getStoreAId());
            order.put("customerId", seed.getCustomerId());
            order.put("status", "PENDING");
            order.put("totalAmount", 100);
            order.put("orderDate", Instant.now().toString());
            order.putArray("items");

            ResponseEntity<String> createOrder = post("/api/orders", cashier.token(),
                    seed.getStoreAId(), seed.getBusinessId(), order);
            boolean expectOrder = RbacSeedData.rolePermissionMap()
                    .getOrDefault("CASHIER", List.of()).contains("order.create");
            int code = createOrder.getStatusCode().value();
            String verdict;
            if (expectOrder && createOrder.getStatusCode().is2xxSuccessful()) verdict = "PASS";
            else if (!expectOrder && (code == 403 || code == 401)) verdict = "PASS";
            else if (createOrder.getStatusCode().is2xxSuccessful() || code == 400 || code == 422)
                verdict = "WARN"; // validation may fail but auth passed
            else verdict = "FAIL";

            probes.add(ProbeResult.of("WRITE", "CASHIER", "POST /api/orders",
                    verdict, code, truncate(createOrder.getBody())));
        }

        // Cashier must not delete store
        if (cashier != null && cashier.ok()) {
            ResponseEntity<String> del = exchange(HttpMethod.DELETE,
                    "/api/stores/" + seed.getStoreBId(), cashier.token(),
                    seed.getStoreAId(), seed.getBusinessId(), null);
            probes.add(ProbeResult.of("WRITE", "CASHIER", "DELETE /api/stores/{id}",
                    (del.getStatusCode().value() == 403 || del.getStatusCode().value() == 401) ? "PASS" : "FAIL",
                    del.getStatusCode().value(), "expect deny store.delete"));
        }
    }

    @Test
    @Order(5)
    void writeExclusiveReport() throws Exception {
        ObjectNode report = objectMapper.createObjectNode();
        report.put("generatedAt", Instant.now().toString());
        report.put("title", "ClappN Thorough System Feature Report");
        report.put("passwordForAllThoroughUsers", ThoroughFeatureSeedService.PASSWORD);

        ObjectNode seedNode = report.putObject("seed");
        seedNode.put("businessId", seed.getBusinessId());
        seedNode.put("storeAId", seed.getStoreAId());
        seedNode.put("storeBId", seed.getStoreBId());
        seedNode.put("productTemplateId", seed.getProductTemplateId());
        seedNode.put("serviceTemplateId", seed.getServiceTemplateId());
        seedNode.put("storeProductId", seed.getStoreProductId());
        seedNode.put("storeServiceId", seed.getStoreServiceId());
        seedNode.put("packageId", seed.getPackageId());
        seedNode.put("customerId", seed.getCustomerId());
        seedNode.put("roleCount", seed.getRoleCount());
        seedNode.put("membershipCount", seed.getMembershipCount());
        seedNode.put("permissionMappedRoles", seed.getPermissionMappedRoles());
        seedNode.set("emailsByRole", objectMapper.valueToTree(seed.getEmailsByRole()));

        long pass = probes.stream().filter(p -> "PASS".equals(p.verdict())).count();
        long fail = probes.stream().filter(p -> "FAIL".equals(p.verdict())).count();
        long warn = probes.stream().filter(p -> "WARN".equals(p.verdict())).count();
        long skip = probes.stream().filter(p -> "SKIP".equals(p.verdict())).count();

        ObjectNode summary = report.putObject("summary");
        summary.put("totalProbes", probes.size());
        summary.put("pass", pass);
        summary.put("fail", fail);
        summary.put("warn", warn);
        summary.put("skip", skip);
        summary.put("passRate", probes.isEmpty() ? 0 : (pass * 100.0 / probes.size()));
        summary.put("loginSuccess", logins.values().stream().filter(LoginOutcome::ok).count());
        summary.put("loginTotal", logins.size());

        ArrayNode results = report.putArray("results");
        for (ProbeResult p : probes) {
            ObjectNode n = results.addObject();
            n.put("category", p.category());
            n.put("role", p.role());
            n.put("action", p.action());
            n.put("verdict", p.verdict());
            n.put("httpStatus", p.httpStatus());
            n.put("detail", p.detail());
        }

        ArrayNode failures = report.putArray("failures");
        probes.stream().filter(p -> "FAIL".equals(p.verdict())).forEach(p -> {
            ObjectNode n = failures.addObject();
            n.put("role", p.role());
            n.put("action", p.action());
            n.put("httpStatus", p.httpStatus());
            n.put("detail", p.detail());
        });

        Path out = Path.of("target", "thorough-system-report.json");
        Files.createDirectories(out.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), report);

        Path md = Path.of("target", "thorough-system-report.md");
        Files.writeString(md, toMarkdown(report));

        assertTrue(Files.exists(out));
        assertTrue(fail < probes.size() / 2, "Too many failures: " + fail + " / " + probes.size()
                + " — see " + out.toAbsolutePath());
    }

    private String toMarkdown(ObjectNode report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ClappN Thorough System Feature Report\n\n");
        sb.append("Generated: ").append(report.get("generatedAt").asText()).append("\n\n");
        JsonNode s = report.get("summary");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n|---|---|\n");
        sb.append("| Total probes | ").append(s.get("totalProbes")).append(" |\n");
        sb.append("| Pass | ").append(s.get("pass")).append(" |\n");
        sb.append("| Fail | ").append(s.get("fail")).append(" |\n");
        sb.append("| Warn | ").append(s.get("warn")).append(" |\n");
        sb.append("| Skip | ").append(s.get("skip")).append(" |\n");
        sb.append("| Pass rate | ").append(String.format("%.1f%%", s.get("passRate").asDouble())).append(" |\n");
        sb.append("| Logins | ").append(s.get("loginSuccess")).append("/").append(s.get("loginTotal")).append(" |\n\n");

        sb.append("## Seed\n\n");
        JsonNode seedNode = report.get("seed");
        sb.append("- Business ID: ").append(seedNode.get("businessId")).append("\n");
        sb.append("- Store A/B: ").append(seedNode.get("storeAId")).append(" / ").append(seedNode.get("storeBId")).append("\n");
        sb.append("- Product/Service templates: ").append(seedNode.get("productTemplateId"))
                .append(" / ").append(seedNode.get("serviceTemplateId")).append("\n");
        sb.append("- Memberships: ").append(seedNode.get("membershipCount")).append("\n");
        sb.append("- Password (all thorough.* users): `").append(ThoroughFeatureSeedService.PASSWORD).append("`\n\n");

        sb.append("## Failures\n\n");
        JsonNode failures = report.get("failures");
        if (failures.isEmpty()) {
            sb.append("_None_\n");
        } else {
            sb.append("| Role | Action | HTTP | Detail |\n|---|---|---|---|\n");
            for (JsonNode f : failures) {
                sb.append("| ").append(f.get("role").asText())
                        .append(" | ").append(f.get("action").asText().replace("|", "/"))
                        .append(" | ").append(f.get("httpStatus").asInt())
                        .append(" | ").append(f.get("detail").asText().replace("|", "/").replace("\n", " "))
                        .append(" |\n");
            }
        }
        return sb.toString();
    }

    private Long storeHeaderFor(String role) {
        if ("SYSTEM_ADMIN".equals(role) || BUSINESS_LEVEL.contains(role)
                || "CUSTOMER".equals(role) || "GUEST".equals(role)) {
            return null;
        }
        return seed.getStoreAId();
    }

    private LoginOutcome login(String email) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("username", email);
            body.put("password", ThoroughFeatureSeedService.PASSWORD);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> resp = rest.exchange(url("/api/auth/login"), HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return LoginOutcome.fail(resp.getStatusCode().value(), truncate(resp.getBody()));
            }
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode token = root.path("data").path("tokenInfo").path("token");
            if (token.isMissingNode() || token.asText().isBlank()) {
                token = root.path("data").path("tokenInfo").path("accessToken");
            }
            if (token.isMissingNode() || token.asText().isBlank()) {
                return LoginOutcome.fail(resp.getStatusCode().value(), truncate(resp.getBody()));
            }
            return LoginOutcome.ok(resp.getStatusCode().value(), token.asText());
        } catch (Exception ex) {
            return LoginOutcome.fail(0, ex.getMessage());
        }
    }

    private ResponseEntity<String> get(String path, String token, Long storeId, Long businessId) {
        return exchange(HttpMethod.GET, path, token, storeId, businessId, null);
    }

    private ResponseEntity<String> post(String path, String token, Long storeId, Long businessId, JsonNode body) {
        return exchange(HttpMethod.POST, path, token, storeId, businessId, body);
    }

    private ResponseEntity<String> exchange(HttpMethod method, String path, String token,
                                            Long storeId, Long businessId, JsonNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (storeId != null) headers.set("X-Store-Id", String.valueOf(storeId));
        if (businessId != null) headers.set("X-Business-Id", String.valueOf(businessId));
        HttpEntity<String> entity = body == null
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(body.toString(), headers);
        try {
            return rest.exchange(url(path), method, entity, String.class);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        String t = s.replace('\n', ' ');
        return t.length() > 180 ? t.substring(0, 180) + "…" : t;
    }

    private record LoginOutcome(boolean ok, int status, String token, String message) {
        static LoginOutcome ok(int status, String token) {
            return new LoginOutcome(true, status, token, "ok");
        }

        static LoginOutcome fail(int status, String message) {
            return new LoginOutcome(false, status, null, message == null ? "" : message);
        }
    }

    private record ProbeResult(String category, String role, String action, String verdict, int httpStatus, String detail) {
        static ProbeResult of(String category, String role, String action, String verdict, int httpStatus, String detail) {
            return new ProbeResult(category, role, action, verdict, httpStatus, detail);
        }
    }
}
