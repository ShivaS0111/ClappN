package biz.craftline.server.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live-environment smoke: login → me/context → store products → order → payment initiate.
 * <p>
 * Not run in default {@code mvn test}. Enable with:
 * <pre>
 *   ./mvnw -Pstaging-smoke -Dtest=StagingSmokeIT test
 * </pre>
 * Required env: {@code STAGING_BASE_URL}, {@code STAGING_EMAIL}, {@code STAGING_PASSWORD},
 * {@code STAGING_STORE_ID}, {@code STAGING_BUSINESS_ID}.
 * Optional: {@code STAGING_SKIP_ORDER=true}, {@code STAGING_PAYMENT_GATEWAY=STRIPE|RAZORPAY},
 * {@code STAGING_CURRENCY=INR}.
 */
@Tag("staging-smoke")
class StagingSmokeIT {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Test
    void login_context_catalog_order_payment() throws Exception {
        String base = env("STAGING_BASE_URL");
        String email = env("STAGING_EMAIL");
        String password = env("STAGING_PASSWORD");
        String storeId = env("STAGING_STORE_ID");
        String businessId = env("STAGING_BUSINESS_ID");

        Assumptions.assumeTrue(notBlank(base), "STAGING_BASE_URL not set — skip staging smoke");
        Assumptions.assumeTrue(notBlank(email), "STAGING_EMAIL not set — skip staging smoke");
        Assumptions.assumeTrue(notBlank(password), "STAGING_PASSWORD not set — skip staging smoke");
        Assumptions.assumeTrue(notBlank(storeId), "STAGING_STORE_ID not set — skip staging smoke");
        Assumptions.assumeTrue(notBlank(businessId), "STAGING_BUSINESS_ID not set — skip staging smoke");

        base = trimSlash(base);

        // 0) health
        HttpResponse<String> health = get(base + "/actuator/health", null, null, null);
        assertEquals(200, health.statusCode(), "health: " + health.body());

        // 1) login
        Map<String, Object> loginBody = Map.of("username", email, "password", password);
        HttpResponse<String> login = postJson(base + "/api/auth/login", loginBody, null, null, null);
        assertEquals(200, login.statusCode(), "login: " + login.body());
        JsonNode loginJson = MAPPER.readTree(login.body());
        assertTrue(loginJson.path("success").asBoolean(false), "login success=false: " + login.body());
        String token = loginJson.path("data").path("tokenInfo").path("token").asText(null);
        assertNotNull(token, "missing JWT in login response");

        // 2) me/context
        HttpResponse<String> me = get(base + "/api/me/context", token, storeId, businessId);
        assertEquals(200, me.statusCode(), "me/context: " + me.body());
        JsonNode meJson = MAPPER.readTree(me.body());
        assertTrue(meJson.path("success").asBoolean(false), "me/context failed: " + me.body());

        boolean skipOrder = "true".equalsIgnoreCase(System.getenv().getOrDefault("STAGING_SKIP_ORDER", "false"));
        if (skipOrder) {
            return;
        }

        // 3) store products
        HttpResponse<String> products = get(base + "/api/store-product", token, storeId, businessId);
        assertEquals(200, products.statusCode(), "store-product: " + products.body());
        JsonNode productList = MAPPER.readTree(products.body()).path("data");
        Assumptions.assumeTrue(productList.isArray() && productList.size() > 0,
                "No store products in staging — assign catalog first");

        JsonNode product = null;
        double unitPrice = 0;
        for (JsonNode p : productList) {
            if (p.path("storeId").asLong(-1) != Long.parseLong(storeId)
                    && p.hasNonNull("storeId")) {
                // prefer matching store when field present
                continue;
            }
            JsonNode priceNode = p.path("price");
            if (priceNode.isObject() && priceNode.path("price").isNumber()) {
                product = p;
                unitPrice = priceNode.path("price").asDouble();
                break;
            }
            if (product == null) {
                product = p;
            }
        }
        assertNotNull(product, "could not pick a store product");
        Assumptions.assumeTrue(unitPrice > 0 || product.path("businessProductId").isNumber(),
                "Pick a product that has store/business price configured");

        long itemId = product.path("id").asLong();
        long businessProductId = product.path("businessProductId").asLong(itemId);

        // 4) place order — no client price
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemType", 1L);
        item.put("itemId", businessProductId > 0 ? businessProductId : itemId);
        item.put("quantity", 1);
        item.put("itemName", product.path("aliasName").asText(product.path("name").asText("smoke-item")));

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("storeId", Long.parseLong(storeId));
        order.put("items", List.of(item));

        HttpResponse<String> orderRes = postJson(base + "/api/orders/new", order, token, storeId, businessId);
        assertEquals(200, orderRes.statusCode(), "place order: " + orderRes.body());
        JsonNode orderJson = MAPPER.readTree(orderRes.body());
        assertTrue(orderJson.path("success").asBoolean(false), "order failed: " + orderRes.body());
        long orderId = orderJson.path("data").path("id").asLong(0);
        assertTrue(orderId > 0, "order id missing: " + orderRes.body());

        double charged = orderJson.path("data").path("totalAmount").asDouble(0);
        assertTrue(charged > 0, "server totalAmount must be > 0");
        long amountMinor = BigDecimal.valueOf(charged)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        // 5) payment initiate (optional — needs real gateway keys on server)
        String gateway = System.getenv("STAGING_PAYMENT_GATEWAY");
        if (!notBlank(gateway)) {
            return;
        }
        String currency = System.getenv().getOrDefault("STAGING_CURRENCY", "INR");
        Map<String, Object> pay = new LinkedHashMap<>();
        pay.put("orderId", orderId);
        pay.put("amount", amountMinor);
        pay.put("currency", currency);
        pay.put("gateway", gateway.trim().toUpperCase());

        HttpResponse<String> payRes = postJson(base + "/api/payments/initiate", pay, token, storeId, businessId);
        assertEquals(200, payRes.statusCode(), "payment initiate: " + payRes.body());
        JsonNode payJson = MAPPER.readTree(payRes.body());
        // initiate may return raw InitiatePaymentResponse (not wrapped) or error string
        if (payJson.has("paymentId")) {
            assertNotNull(payJson.path("paymentId").asText(null));
        } else if (payJson.has("success")) {
            assertTrue(payJson.path("success").asBoolean(false), "payment: " + payRes.body());
        }
    }

    private static String env(String name) {
        String v = System.getenv(name);
        return v == null ? "" : v.trim();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String trimSlash(String base) {
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private HttpResponse<String> get(String url, String token, String storeId, String businessId) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET();
        applyAuth(b, token, storeId, businessId);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String url, Object body, String token, String storeId, String businessId)
            throws Exception {
        byte[] json = MAPPER.writeValueAsBytes(body);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(json));
        applyAuth(b, token, storeId, businessId);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void applyAuth(HttpRequest.Builder b, String token, String storeId, String businessId) {
        if (notBlank(token)) {
            b.header("Authorization", "Bearer " + token);
        }
        if (notBlank(storeId)) {
            b.header("X-Store-Id", storeId);
        }
        if (notBlank(businessId)) {
            b.header("X-Business-Id", businessId);
        }
    }
}
