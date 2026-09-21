package biz.craftline.server.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Post-deploy smoke against a live environment (staging by default).
 * Flow: health → login → /api/me/context → scoped store products → order (optional) → webhook reject.
 *
 * Run: {@code mvnw -Pstaging-smoke test}
 * Env: STAGING_BASE_URL, STAGING_EMAIL, STAGING_PASSWORD [, STAGING_STORE_ID, STAGING_BUSINESS_ID]
 */
@Tag("staging-smoke")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StagingSmokeIT {

    private final ObjectMapper json = new ObjectMapper();
    private final List<String> log = new ArrayList<>();

    @Test
    void stagingSmoke() throws Exception {
        Assumptions.assumeTrue(StagingSmokeConfig.isConfigured(),
                "Set STAGING_EMAIL and STAGING_PASSWORD to run staging smoke");

        StagingHttp http = new StagingHttp(StagingSmokeConfig.BASE_URL);
        recordStep("target", StagingSmokeConfig.BASE_URL);

        // 1. Health
        StagingHttp.Response health = http.get("/actuator/health", null, null, null);
        recordStep("health", health.status() + " " + truncate(health.body()));
        assertTrue(health.status() == 200 || health.status() == 503,
                "actuator/health unreachable: " + health.status());

        // 2. Login
        String loginBody = json.createObjectNode()
                .put("username", StagingSmokeConfig.EMAIL)
                .put("password", StagingSmokeConfig.PASSWORD)
                .toString();
        StagingHttp.Response login = http.post("/api/auth/login", null, null, null, loginBody);
        recordStep("login", login.status() + " " + truncate(login.body()));
        assertEquals(200, login.status(), "login failed: " + login.body());

        String token = extractToken(login.body());
        assertNotNull(token, "JWT missing in login response");

        // 3. Context
        StagingHttp.Response context = http.get("/api/me/context", token, null, null);
        recordStep("me/context", context.status() + " " + truncate(context.body()));
        assertEquals(200, context.status(), "/api/me/context failed");

        Long storeId = parseLongEnv(StagingSmokeConfig.STORE_ID);
        Long businessId = parseLongEnv(StagingSmokeConfig.BUSINESS_ID);
        if (storeId == null) {
            storeId = firstIdFromContext(context.body(), "accessibleStoreIds");
            if (storeId == null) {
                storeId = firstIdFromContext(context.body(), "effectiveStoreIds");
            }
        }
        if (businessId == null) {
            businessId = firstIdFromContext(context.body(), "accessibleBusinessIds");
            if (businessId == null) {
                businessId = firstIdFromContext(context.body(), "effectiveBusinessIds");
            }
        }
        assertNotNull(storeId, "No store scope — set STAGING_STORE_ID or use a scoped user");
        recordStep("scope", "storeId=" + storeId + " businessId=" + businessId);

        // 4. Scoped store list
        StagingHttp.Response stores = http.get("/api/stores/list", token, storeId, businessId);
        recordStep("stores/list", stores.status() + " " + truncate(stores.body()));
        assertEquals(200, stores.status(), "stores list failed");

        // 5. Scoped products for store
        StagingHttp.Response products = http.get("/api/store-product/store/" + storeId, token, storeId, businessId);
        recordStep("store-product", products.status() + " " + truncate(products.body()));
        assertEquals(200, products.status(), "store products failed");

        // 6. Optional order placement
        if (!StagingSmokeConfig.SKIP_ORDER) {
            String orderPayload = buildMinimalOrder(storeId, products.body());
            StagingHttp.Response order = http.post("/api/orders/new", token, storeId, businessId, orderPayload);
            recordStep("order/new", order.status() + " " + truncate(order.body()));
            assertTrue(order.status() == 200 || order.status() == 201 || order.status() == 400,
                    "unexpected order status: " + order.status());
        }

        // 7. Webhook must reject unsigned payload
        StagingHttp.Response webhook = http.postRaw(
                "/api/payments/webhook/stripe",
                "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"smoke\"}}}",
                Map.of("stripe-signature", "invalid-smoke-signature"));
        recordStep("webhook/stripe", webhook.status() + " " + truncate(webhook.body()));
        assertEquals(401, webhook.status(), "unsigned webhook must return 401");

        System.out.println("=== Staging smoke PASSED ===");
        log.forEach(line -> System.out.println("  " + line));
    }

    private void recordStep(String name, String detail) {
        log.add(name + ": " + detail);
    }

    private String extractToken(String body) throws Exception {
        JsonNode data = json.readTree(body).path("data");
        JsonNode tokenInfo = data.path("tokenInfo");
        if (tokenInfo.has("token") && !tokenInfo.get("token").asText().isBlank()) {
            return tokenInfo.get("token").asText();
        }
        if (data.has("accessToken")) {
            return data.get("accessToken").asText();
        }
        if (data.has("token")) {
            return data.get("token").asText();
        }
        return null;
    }

    private Long firstIdFromContext(String body, String field) throws Exception {
        JsonNode arr = json.readTree(body).path("data").path(field);
        if (arr.isArray() && !arr.isEmpty()) {
            return arr.get(0).asLong();
        }
        return null;
    }

    private Long parseLongEnv(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }

    private String buildMinimalOrder(Long storeId, String productsBody) throws Exception {
        var order = json.createObjectNode();
        order.put("storeId", storeId);
        order.put("status", "CREATED");
        order.put("totalAmount", 1);
        var items = order.putArray("items");
        JsonNode products = json.readTree(productsBody).path("data");
        if (products.isArray() && !products.isEmpty()) {
            JsonNode p = products.get(0);
            var item = items.addObject();
            item.put("itemType", 1);
            item.put("itemIId", p.path("id").asLong(1));
            item.put("quantity", 1);
            item.put("price", 1.0);
        }
        return order.toString();
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + "…" : s;
    }
}
