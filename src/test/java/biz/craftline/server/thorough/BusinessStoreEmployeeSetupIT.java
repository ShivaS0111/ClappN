package biz.craftline.server.thorough;

import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * End-to-end setup flow, driven through the HTTP API as SYSTEM_ADMIN:
 * business (+ auto-created owner) -> two stores -> business-level employees -> store-level employees.
 * Every run uses a unique suffix so it can be repeated against the same MySQL database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("thorough")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("thorough")
class BusinessStoreEmployeeSetupIT {

    private static final String PASSWORD = "Test@12345";

    private static final List<String> BUSINESS_ROLES = List.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER");

    private static final List<String> STORE_ROLES = List.of(
            "STORE_OWNER", "STORE_MANAGER", "CASHIER", "INVENTORY_MANAGER", "CUSTOMER_SERVICE_REP");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final String suffix = Long.toString(System.currentTimeMillis());
    private final Map<String, Long> roleIds = new HashMap<>();
    /** email -> expected role name, for every employee created by this test. */
    private final Map<String, String> expectedRoleByEmail = new LinkedHashMap<>();
    private final List<Long> storeIds = new java.util.ArrayList<>();

    private String adminToken;
    private Long businessId;
    private String ownerEmail;
    private Long businessTypeId;

    @BeforeAll
    void setUp() throws Exception {
        for (String roleName : concat(List.of("SYSTEM_ADMIN"), BUSINESS_ROLES, STORE_ROLES)) {
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new AssertionError("Role not seeded: " + roleName));
            roleIds.put(roleName, role.getId());
        }

        String adminEmail = "setup.admin." + suffix + "@clapp.test";
        UserEntity admin = new UserEntity();
        admin.setFullName("Setup Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        admin.setEnabled(true);
        admin.setVerified(1);
        admin.setAccountNonLocked(true);
        admin.setAccountNonExpired(true);
        admin.setCredentialsNonExpired(true);
        admin.setRoles(new HashSet<>());
        admin.getRoles().add(roleRepository.findById(roleIds.get("SYSTEM_ADMIN")).orElseThrow());
        userRepository.save(admin);

        adminToken = login(adminEmail);
        assertNotNull(adminToken, "SYSTEM_ADMIN login must return a token");

        JsonNode types = getOk("/api/business-type/list");
        assertTrue(types.isArray() && types.size() > 0, "at least one business type must be seeded");
        businessTypeId = types.get(0).path("id").asLong();
        for (JsonNode type : types) {
            if (type.path("id").asLong() == 1L) {
                businessTypeId = 1L;
            }
        }
    }

    @Test
    @Order(1)
    void createBusiness() throws Exception {
        ownerEmail = email("primary.owner");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("businessName", "Sample Retail Co " + suffix);
        body.put("description", "Sample business created by BusinessStoreEmployeeSetupIT");
        body.put("status", 1);
        body.put("contact", "+1-555-0100");
        body.put("email", email("business"));
        body.put("website", "https://sample-retail.example.com");
        body.put("address", "1 Market Street, Springfield");
        body.put("latitude", 40.7128);
        body.put("longitude", -74.0060);
        body.put("ownerName", "Primary Owner");
        body.put("ownerEmail", ownerEmail);
        body.put("ownerPhone", "+1-555-0101");
        body.put("ownerPassword", PASSWORD);

        JsonNode data = postOk("/api/business", body, 200);
        businessId = data.path("id").asLong();
        assertTrue(businessId > 0, "business id must be returned");
        assertEquals("Sample Retail Co " + suffix, data.path("businessName").asText());

        // Business creation auto-provisions a BUSINESS_OWNER employee
        expectedRoleByEmail.put(ownerEmail, "BUSINESS_OWNER");
    }

    @Test
    @Order(2)
    void createTwoStoresUnderBusiness() throws Exception {
        assertNotNull(businessId, "business must exist (createBusiness)");

        for (int i = 1; i <= 2; i++) {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("storeName", "Sample Store " + i + " " + suffix);
            body.put("description", "Sample store " + i);
            body.put("status", 1);
            body.put("businessId", businessId);
            body.put("businessType", businessTypeId);
            body.put("address", i + "00 Main Street, Springfield");
            body.put("email", email("store" + i));
            body.put("phone", "+1-555-020" + i);
            body.put("hours", "09:00-21:00");

            JsonNode data = postOk("/api/stores", body, 201);
            long storeId = data.path("id").asLong();
            assertTrue(storeId > 0, "store id must be returned");
            assertEquals(businessId, data.path("business").path("id").asLong(),
                    "store must belong to the new business");
            storeIds.add(storeId);
        }

        assertEquals(2, storeIds.size());
        assertTrue(!storeIds.get(0).equals(storeIds.get(1)), "stores must be distinct");
    }

    @Test
    @Order(3)
    void createBusinessLevelEmployees() throws Exception {
        assertNotNull(businessId, "business must exist (createBusiness)");

        for (String role : BUSINESS_ROLES) {
            String email = email("biz." + role.toLowerCase());
            ObjectNode body = employeeBody(role, email);
            body.put("businessId", businessId);

            JsonNode data = postOk("/api/employees", body, 200);
            assertEquals(roleIds.get(role).longValue(), data.path("roleId").asLong(),
                    "role mismatch for " + role);
            assertEquals(businessId, data.path("businessId").asLong());
            assertTrue(data.path("storeId").isMissingNode() || data.path("storeId").isNull(),
                    "business-level employee must not be store-scoped");
            expectedRoleByEmail.put(email, role);
        }
    }

    @Test
    @Order(4)
    void createStoreLevelEmployeesForEachStore() throws Exception {
        assertEquals(2, storeIds.size(), "stores must exist (createTwoStoresUnderBusiness)");

        for (int s = 0; s < storeIds.size(); s++) {
            Long storeId = storeIds.get(s);
            for (String role : STORE_ROLES) {
                String email = email("store" + (s + 1) + "." + role.toLowerCase());
                ObjectNode body = employeeBody(role, email);
                body.put("storeId", storeId);

                JsonNode data = postOk("/api/employees", body, 200);
                assertEquals(roleIds.get(role).longValue(), data.path("roleId").asLong(),
                        "role mismatch for " + role);
                assertEquals(storeId, data.path("storeId").asLong());
                assertEquals(businessId, data.path("businessId").asLong(),
                        "business must be resolved from the store");
                expectedRoleByEmail.put(email, role);
            }
        }
    }

    @Test
    @Order(5)
    void verifyFinalStructure() throws Exception {
        // Business-level view: the primary owner + 3 business roles + 2 stores x 5 store roles
        JsonNode all = getOk("/api/employees/business/" + businessId);
        int expected = 1 + BUSINESS_ROLES.size() + storeIds.size() * STORE_ROLES.size();
        assertEquals(expected, all.size(), "total employees in business");
        assertEquals(expected, expectedRoleByEmail.size());

        Map<String, JsonNode> byEmail = new HashMap<>();
        for (JsonNode e : all) {
            byEmail.put(e.path("email").asText(), e);
        }
        for (Map.Entry<String, String> exp : expectedRoleByEmail.entrySet()) {
            JsonNode e = byEmail.get(exp.getKey());
            assertNotNull(e, "employee missing: " + exp.getKey());
            assertEquals(roleIds.get(exp.getValue()).longValue(), e.path("roleId").asLong(),
                    "wrong role for " + exp.getKey());
            assertEquals("ACTIVE", e.path("status").asText().toUpperCase(),
                    "employee must be active: " + exp.getKey());
        }

        // Store-level view: each store only has its own five employees
        for (Long storeId : storeIds) {
            JsonNode storeEmployees = getOk("/api/employees/store/" + storeId);
            assertEquals(STORE_ROLES.size(), storeEmployees.size(), "employees in store " + storeId);
            for (JsonNode e : storeEmployees) {
                assertEquals(storeId, e.path("storeId").asLong());
                assertTrue(STORE_ROLES.contains(expectedRoleByEmail.get(e.path("email").asText())),
                        "unexpected employee in store " + storeId + ": " + e.path("email").asText());
            }
        }

        // Business has exactly the two created stores
        JsonNode stores = getOk("/api/stores/list/" + businessId);
        assertEquals(2, stores.size(), "stores under business");
    }

    // ======================================================================
    // Catalog -> store assignment -> inventory -> purchase -> order -> invoice
    // ======================================================================

    private static final String[] PRODUCT_NAMES = {"Espresso Beans 1kg", "Ceramic Mug", "Reusable Tote Bag"};
    private static final double[] PRODUCT_PRICES = {12.50, 8.00, 5.00};
    private static final String[] SERVICE_NAMES = {"Coffee Tasting Session", "Barista Workshop"};
    private static final double[] SERVICE_PRICES = {25.00, 40.00};
    private static final long[] SERVICE_MINUTES = {30, 60};

    /** Item type the order service treats as a physical product (inventory is allocated for it). */
    private static final long ORDER_ITEM_PRODUCT = 1L;
    /** Any non-1 item type skips inventory allocation; used for service lines. */
    private static final long ORDER_ITEM_SERVICE = 2L;

    private final List<Long> productIds = new java.util.ArrayList<>();
    private final List<Long> serviceIds = new java.util.ArrayList<>();
    /** store index -> store-product ids, aligned with productIds. */
    private final Map<Integer, List<Long>> storeProductIds = new HashMap<>();
    /** store index -> store-service ids, aligned with serviceIds. */
    private final Map<Integer, List<Long>> storeServiceIds = new HashMap<>();
    private final Map<String, String> actorTokens = new HashMap<>();
    private final Map<String, Long> lotIds = new HashMap<>();
    private Long customerId;
    private Long order1Id;
    private Long order2Id;
    private Long invoice1Id;

    @Test
    @Order(6)
    void createBusinessLevelProductsAndServices() throws Exception {
        assertNotNull(businessId, "business must exist");

        for (int i = 0; i < PRODUCT_NAMES.length; i++) {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", PRODUCT_NAMES[i] + " " + suffix);
            body.put("desc", "Sample product " + (i + 1));
            body.put("status", 1);
            body.put("businessId", businessId);
            body.put("businessTypeId", businessTypeId);
            body.put("amount", PRODUCT_PRICES[i]);
            JsonNode data = ok(HttpMethod.POST, "/api/business-product", adminToken, null, body);
            assertTrue(data.path("id").asLong() > 0, "business product id");
            productIds.add(data.path("id").asLong());
        }
        for (int i = 0; i < SERVICE_NAMES.length; i++) {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", SERVICE_NAMES[i] + " " + suffix);
            body.put("desc", "Sample service " + (i + 1));
            body.put("status", 1);
            body.put("businessId", businessId);
            body.put("businessTypeId", businessTypeId);
            body.put("amount", SERVICE_PRICES[i]);
            body.put("duration", SERVICE_MINUTES[i]);
            JsonNode data = ok(HttpMethod.POST, "/api/business-service", adminToken, null, body);
            assertTrue(data.path("id").asLong() > 0, "business service id");
            serviceIds.add(data.path("id").asLong());
        }

        JsonNode products = asList(getOk("/api/business-product/listByBusiness/" + businessId));
        assertEquals(PRODUCT_NAMES.length, products.size(), "products in business master catalog");
        for (int i = 0; i < productIds.size(); i++) {
            JsonNode p = findById(products, "id", productIds.get(i));
            assertNotNull(p, "product missing from master catalog: " + productIds.get(i));
            assertEquals(PRODUCT_NAMES[i] + " " + suffix, p.path("name").asText());
            assertEquals(PRODUCT_PRICES[i], p.path("amount").asDouble(), 0.001);
            assertEquals(businessId, p.path("businessId").asLong());
            assertEquals(businessTypeId, p.path("businessType").path("id").asLong());
            assertEquals(PRODUCT_NAMES[i] + " " + suffix,
                    getOk("/api/business-product/" + productIds.get(i)).path("name").asText());
        }

        JsonNode services = asList(getOk("/api/business-service/listByBusiness/" + businessId));
        assertEquals(SERVICE_NAMES.length, services.size(), "services in business master catalog");
        for (int i = 0; i < serviceIds.size(); i++) {
            JsonNode s = findById(services, "id", serviceIds.get(i));
            assertNotNull(s, "service missing from master catalog: " + serviceIds.get(i));
            assertEquals(SERVICE_NAMES[i] + " " + suffix, s.path("name").asText());
            assertEquals(SERVICE_PRICES[i], s.path("amount").asDouble(), 0.001);
            assertEquals(SERVICE_MINUTES[i], s.path("duration").asLong());
            assertEquals(businessId, s.path("businessId").asLong());
        }
    }

    @Test
    @Order(7)
    void assignCatalogToBothStores() throws Exception {
        assertEquals(PRODUCT_NAMES.length, productIds.size(), "products must exist");
        assertEquals(SERVICE_NAMES.length, serviceIds.size(), "services must exist");

        for (int s = 0; s < storeIds.size(); s++) {
            storeProductIds.put(s, new java.util.ArrayList<>());
            storeServiceIds.put(s, new java.util.ArrayList<>());
        }

        for (int i = 0; i < productIds.size(); i++) {
            JsonNode created = ok(HttpMethod.POST, "/api/store-product/assign", adminToken, null,
                    assignBody(productIds.get(i), PRODUCT_NAMES[i]));
            assertEquals(storeIds.size(), created.size(), "one store product per store");
            for (JsonNode sp : created) {
                assertEquals(productIds.get(i).longValue(), sp.path("businessProductId").asLong(),
                        "store product must reference the master product");
            }
        }
        for (int i = 0; i < serviceIds.size(); i++) {
            JsonNode created = ok(HttpMethod.POST, "/api/store-service/assign", adminToken, null,
                    assignBody(serviceIds.get(i), SERVICE_NAMES[i]));
            assertEquals(storeIds.size(), created.size(), "one store service per store");
            for (JsonNode ss : created) {
                assertEquals(serviceIds.get(i).longValue(), ss.path("businessServiceId").asLong(),
                        "store service must reference the master service");
            }
        }

        for (int s = 0; s < storeIds.size(); s++) {
            Long storeId = storeIds.get(s);
            JsonNode products = asList(getOk("/api/store-product/store/" + storeId));
            assertEquals(productIds.size(), products.size(), "store " + storeId + " product catalog");
            for (Long masterId : productIds) {
                JsonNode sp = findById(products, "businessProductId", masterId);
                assertNotNull(sp, "store " + storeId + " is missing master product " + masterId);
                assertEquals(storeId.longValue(), sp.path("storeId").asLong());
                assertEquals(businessId, sp.path("businessId").asLong());
                assertEquals(1, sp.path("status").asInt(), "assigned product must be active");
                storeProductIds.get(s).add(sp.path("id").asLong());
            }
            JsonNode services = asList(getOk("/api/store-service/store/" + storeId));
            assertEquals(serviceIds.size(), services.size(), "store " + storeId + " service catalog");
            for (Long masterId : serviceIds) {
                JsonNode ss = findById(services, "businessServiceId", masterId);
                assertNotNull(ss, "store " + storeId + " is missing master service " + masterId);
                assertEquals(storeId.longValue(), ss.path("storeId").asLong());
                storeServiceIds.get(s).add(ss.path("id").asLong());
            }
        }

        // The two stores hold distinct store-level records for the same master items
        assertTrue(!storeProductIds.get(0).equals(storeProductIds.get(1)), "store products are per store");

        // Assigning must reference the master, not copy it: master catalog is unchanged
        assertEquals(productIds.size(),
                asList(getOk("/api/business-product/listByBusiness/" + businessId)).size(),
                "assignment must not create business-level products");
        assertEquals(serviceIds.size(),
                asList(getOk("/api/business-service/listByBusiness/" + businessId)).size(),
                "assignment must not create business-level services");

        // Re-assigning is idempotent: no duplicate store records
        ok(HttpMethod.POST, "/api/store-product/assign", adminToken, null,
                assignBody(productIds.get(0), PRODUCT_NAMES[0]));
        assertEquals(productIds.size(), asList(getOk("/api/store-product/store/" + storeIds.get(0))).size(),
                "re-assign must not duplicate store products");
    }

    @Test
    @Order(8)
    void addAndAdjustStoreInventory() throws Exception {
        assertEquals(2, storeProductIds.size(), "catalog must be assigned to stores");
        Long s1 = storeIds.get(0);
        Long s2 = storeIds.get(1);
        String inv1 = actorToken(0, "INVENTORY_MANAGER");
        String inv2 = actorToken(1, "INVENTORY_MANAGER");

        // Before: no stock records at all
        for (int s = 0; s < 2; s++) {
            Map<Long, JsonNode> before = inventory(s);
            for (Long spId : storeProductIds.get(s)) {
                assertTrue(!before.containsKey(spId), "no inventory expected before stock is added");
            }
        }

        // Store 1: product 1 in two lots (FEFO test), product 2 in one lot, product 3 deliberately has no stock
        addLot(inv1, s1, storeProductIds.get(0).get(0), "S1-P1-A", 6, PRODUCT_PRICES[0], "2027-06-30");
        addLot(inv1, s1, storeProductIds.get(0).get(0), "S1-P1-B", 4, PRODUCT_PRICES[0], "2028-06-30");
        addLot(inv1, s1, storeProductIds.get(0).get(1), "S1-P2-A", 5, PRODUCT_PRICES[1], "2028-06-30");

        // Store 2: all three products stocked
        addLot(inv2, s2, storeProductIds.get(1).get(0), "S2-P1-A", 8, PRODUCT_PRICES[0], "2028-06-30");
        addLot(inv2, s2, storeProductIds.get(1).get(1), "S2-P2-A", 3, PRODUCT_PRICES[1], "2028-06-30");
        addLot(inv2, s2, storeProductIds.get(1).get(2), "S2-P3-A", 2, PRODUCT_PRICES[2], "2028-06-30");

        Map<Long, JsonNode> stock1 = inventory(0);
        assertStock(stock1, storeProductIds.get(0).get(0), 10, 10, 0, 0);
        assertStock(stock1, storeProductIds.get(0).get(1), 5, 5, 0, 0);
        assertTrue(!stock1.containsKey(storeProductIds.get(0).get(2)), "store 1 product 3 must have no stock");
        for (JsonNode row : stock1.values()) {
            assertEquals(s1.longValue(), row.path("storeId").asLong(), "inventory must be tied to its store");
        }

        // Lots are tied to the right store and store product
        JsonNode lots = asList(ok(HttpMethod.GET,
                "/api/product-lots/all/" + storeProductIds.get(0).get(0), inv1, s1, null));
        assertEquals(2, lots.size());
        for (JsonNode lot : lots) {
            assertEquals(s1.longValue(), lot.path("storeId").asLong());
            assertEquals(storeProductIds.get(0).get(0).longValue(), lot.path("productId").asLong());
        }

        // Inventory operation: +2 adjustment on a lot, verified before and after at lot and store level
        Long adjustLot = lotIds.get("S2-P3-A");
        Long s2p3 = storeProductIds.get(1).get(2);
        assertStock(inventory(1), s2p3, 2, 2, 0, 0);
        ok(HttpMethod.POST, "/api/product-lots/" + adjustLot + "/transaction?type=ADJUST&quantity=2&reason=recount",
                inv2, s2, null);
        assertStock(inventory(1), s2p3, 4, 4, 0, 0);
        assertEquals(4, ok(HttpMethod.GET, "/api/product-lots/" + adjustLot, inv2, s2, null)
                .path("quantity").asInt(), "lot quantity after adjustment");
    }

    @Test
    @Order(9)
    void verifyCustomerAvailabilityAndOutOfStockRejection() throws Exception {
        Long s1 = storeIds.get(0);
        String cashier = actorToken(0, "CASHIER");

        // Customer is created by the cashier (customers are records, not login users)
        ObjectNode customer = objectMapper.createObjectNode();
        customer.put("storeId", s1);
        customer.put("firstName", "Casey");
        customer.put("lastName", "Customer");
        customer.put("email", email("customer"));
        customer.put("phone", "+1-555-0400");
        customer.put("status", 1);
        JsonNode created = ok(HttpMethod.POST, "/api/customers", cashier, s1, customer);
        customerId = created.path("id").asLong();
        assertTrue(customerId > 0, "customer id");

        // Browse: catalog entries that are active AND in stock are purchasable
        JsonNode catalog = asList(ok(HttpMethod.GET, "/api/store-product/store/" + s1, cashier, s1, null));
        Map<Long, JsonNode> stock = inventory(0);
        List<Long> purchasable = new java.util.ArrayList<>();
        for (JsonNode sp : catalog) {
            long id = sp.path("id").asLong();
            if (sp.path("status").asInt() == 1 && stock.containsKey(id) && stock.get(id).path("available").asInt() > 0) {
                purchasable.add(id);
            }
        }
        java.util.Collections.sort(purchasable);
        List<Long> expected = new java.util.ArrayList<>(
                List.of(storeProductIds.get(0).get(0), storeProductIds.get(0).get(1)));
        java.util.Collections.sort(expected);
        assertEquals(expected, purchasable, "only stocked products are purchasable; product 3 is out of stock");

        // Services are visible in the store and active (services carry no stock)
        JsonNode services = asList(getOk("/api/store-service/store/" + s1));
        for (JsonNode ss : services) {
            assertEquals(1, ss.path("status").asInt(), "service must be active");
        }

        // Out-of-stock product cannot be purchased and leaves nothing behind
        int ordersBefore = asList(ok(HttpMethod.GET, "/api/orders/store/" + s1, cashier, s1, null)).size();
        MvcResult rejected = placeOrder(cashier, s1, List.of(
                item(ORDER_ITEM_PRODUCT, storeProductIds.get(0).get(2), 1, PRODUCT_PRICES[2], PRODUCT_NAMES[2])));
        assertRejected(rejected, "out-of-stock purchase");
        assertEquals(ordersBefore, asList(ok(HttpMethod.GET, "/api/orders/store/" + s1, cashier, s1, null)).size(),
                "rejected order must not be persisted");
        assertTrue(!inventory(0).containsKey(storeProductIds.get(0).get(2)), "no stock row created by a rejected order");
    }

    @Test
    @Order(10)
    void customerPurchaseFlow() throws Exception {
        Long s1 = storeIds.get(0);
        String cashier = actorToken(0, "CASHIER");
        Long p1 = storeProductIds.get(0).get(0);
        Long p2 = storeProductIds.get(0).get(1);
        Long svc1 = storeServiceIds.get(0).get(0);

        // Over-quantity: the whole order is rejected and nothing is reserved (atomic rollback)
        MvcResult tooMany = placeOrder(cashier, s1, List.of(
                item(ORDER_ITEM_PRODUCT, p2, 1, PRODUCT_PRICES[1], PRODUCT_NAMES[1]),
                item(ORDER_ITEM_PRODUCT, p1, 11, PRODUCT_PRICES[0], PRODUCT_NAMES[0])));
        assertRejected(tooMany, "over-quantity purchase");
        Map<Long, JsonNode> unchanged = inventory(0);
        assertStock(unchanged, p1, 10, 10, 0, 0);
        assertStock(unchanged, p2, 5, 5, 0, 0);

        // Valid purchase: 2 x product 1, 1 x product 2, 1 x service
        ObjectNode body = orderBody(s1, List.of(
                item(ORDER_ITEM_PRODUCT, p1, 2, PRODUCT_PRICES[0], PRODUCT_NAMES[0]),
                item(ORDER_ITEM_PRODUCT, p2, 1, PRODUCT_PRICES[1], PRODUCT_NAMES[1]),
                item(ORDER_ITEM_SERVICE, svc1, 1, SERVICE_PRICES[0], SERVICE_NAMES[0])));
        body.put("subtotal", 58.0);
        body.put("totalGst", 0.0);
        body.put("totalDiscount", 0.0);
        JsonNode order = ok(HttpMethod.POST, "/api/orders/new", cashier, s1, body);
        order1Id = order.path("id").asLong();
        assertTrue(order1Id > 0, "order id");
        assertEquals("BLOCKED", order.path("status").asText(), "stock is reserved when the order is placed");
        assertEquals(58.0, order.path("totalAmount").asDouble(), 0.001);

        // Reserved, not yet sold
        Map<Long, JsonNode> reserved = inventory(0);
        assertStock(reserved, p1, 10, 8, 2, 0);
        assertStock(reserved, p2, 5, 4, 1, 0);
    }

    @Test
    @Order(11)
    void completeOrderAndVerifyInventoryDeduction() throws Exception {
        Long s1 = storeIds.get(0);
        Long p1 = storeProductIds.get(0).get(0);
        Long p2 = storeProductIds.get(0).get(1);
        String cashier = actorToken(0, "CASHIER");
        String manager = actorToken(0, "STORE_MANAGER");

        // Cashier cannot complete orders (no order.update)
        assertEquals(403, call(HttpMethod.POST, "/api/orders/" + order1Id + "/complete", cashier, s1, null)
                .getResponse().getStatus(), "cashier must not complete orders");

        ok(HttpMethod.POST, "/api/orders/" + order1Id + "/complete", manager, s1, null);
        assertEquals("COMPLETED", ok(HttpMethod.GET, "/api/orders/" + order1Id, manager, s1, null)
                .path("status").asText());

        Map<Long, JsonNode> after = inventory(0);
        assertStock(after, p1, 10, 8, 0, 2);
        assertStock(after, p2, 5, 4, 0, 1);

        // FEFO: the earliest-expiring lot is consumed first
        Map<String, JsonNode> lots = lotsByCode(0, p1);
        assertEquals(2, lots.get("S1-P1-A").path("sold").asInt(), "earliest-expiry lot sold from first");
        assertEquals(0, lots.get("S1-P1-A").path("blocked").asInt());
        assertEquals(0, lots.get("S1-P1-B").path("sold").asInt());

        // Completing again is rejected
        assertRejected(call(HttpMethod.POST, "/api/orders/" + order1Id + "/complete", manager, s1, null),
                "completing a completed order");
    }

    @Test
    @Order(12)
    void validateOrderContents() throws Exception {
        Long s1 = storeIds.get(0);
        String cashier = actorToken(0, "CASHIER");

        JsonNode order = ok(HttpMethod.GET, "/api/orders/" + order1Id, cashier, s1, null);
        assertEquals(order1Id.longValue(), order.path("id").asLong());
        assertEquals(s1.longValue(), order.path("storeId").asLong(), "order store");
        assertTrue(order.path("storeName").asText().contains("Sample Store 1"), "order store name");
        assertEquals(customerId.longValue(), order.path("customerId").asLong(), "order customer");
        assertEquals("Casey", order.path("customer").path("firstName").asText(), "order customer details");
        assertEquals("COMPLETED", order.path("status").asText());
        assertEquals(58.0, order.path("subtotal").asDouble(), 0.001);
        assertEquals(0.0, order.path("totalGst").asDouble(), 0.001);
        assertEquals(0.0, order.path("totalDiscount").asDouble(), 0.001);
        assertEquals(58.0, order.path("totalAmount").asDouble(), 0.001);

        JsonNode items = order.path("items");
        assertEquals(3, items.size(), "order lines");
        double computed = 0;
        for (JsonNode it : items) {
            computed += it.path("price").asDouble() * it.path("quantity").asInt();
        }
        assertEquals(order.path("totalAmount").asDouble(), computed, 0.001, "total = sum(price x quantity)");

        assertLine(items, ORDER_ITEM_PRODUCT, storeProductIds.get(0).get(0), 2, PRODUCT_PRICES[0], PRODUCT_NAMES[0]);
        assertLine(items, ORDER_ITEM_PRODUCT, storeProductIds.get(0).get(1), 1, PRODUCT_PRICES[1], PRODUCT_NAMES[1]);
        assertLine(items, ORDER_ITEM_SERVICE, storeServiceIds.get(0).get(0), 1, SERVICE_PRICES[0], SERVICE_NAMES[0]);

        // Unit prices on the order match the master catalog price
        assertEquals(PRODUCT_PRICES[0],
                getOk("/api/business-product/" + productIds.get(0)).path("amount").asDouble(), 0.001);

        // Order is discoverable by store and by customer, and only in its own store
        assertNotNull(findById(asList(ok(HttpMethod.GET, "/api/orders/store/" + s1, cashier, s1, null)), "id", order1Id));
        assertNotNull(findById(asList(ok(HttpMethod.GET, "/api/orders/customer/" + customerId, cashier, s1, null)), "id", order1Id));
        String cashier2 = actorToken(1, "CASHIER");
        assertEquals(0, asList(ok(HttpMethod.GET, "/api/orders/store/" + storeIds.get(1), cashier2, storeIds.get(1), null)).size(),
                "store 2 has no orders");
    }

    @Test
    @Order(13)
    void sellOutProductAndVerifyZeroStockBehaviour() throws Exception {
        Long s1 = storeIds.get(0);
        Long p1 = storeProductIds.get(0).get(0);
        Long p2 = storeProductIds.get(0).get(1);
        String cashier = actorToken(0, "CASHIER");
        String manager = actorToken(0, "STORE_MANAGER");
        String invMgr = actorToken(0, "INVENTORY_MANAGER");

        // Buy everything that is left of product 2 (4 units)
        JsonNode order = ok(HttpMethod.POST, "/api/orders/new", cashier, s1, orderBody(s1, List.of(
                item(ORDER_ITEM_PRODUCT, p2, 4, PRODUCT_PRICES[1], PRODUCT_NAMES[1]))));
        order2Id = order.path("id").asLong();
        assertEquals("BLOCKED", order.path("status").asText());
        assertEquals(32.0, order.path("totalAmount").asDouble(), 0.001);
        assertStock(inventory(0), p2, 5, 0, 4, 1);

        ok(HttpMethod.POST, "/api/orders/" + order2Id + "/complete", manager, s1, null);
        assertStock(inventory(0), p2, 5, 0, 0, 5);

        // Zero stock is reported as low stock (available < threshold); product 1 (8 left) is not
        JsonNode low = asList(ok(HttpMethod.GET, "/api/store-inventory/" + s1 + "/low-stock?threshold=1", invMgr, s1, null));
        assertNotNull(findById(low, "productId", p2), "sold-out product must be listed as low stock");
        assertTrue(findById(low, "productId", p1) == null, "in-stock product must not be listed as low stock");

        // Sold-out product can no longer be purchased
        assertRejected(placeOrder(cashier, s1, List.of(
                item(ORDER_ITEM_PRODUCT, p2, 1, PRODUCT_PRICES[1], PRODUCT_NAMES[1]))), "purchase of sold-out product");
        assertStock(inventory(0), p2, 5, 0, 0, 5);
    }

    @Test
    @Order(14)
    void generateAndValidateInvoice() throws Exception {
        Long s1 = storeIds.get(0);
        String cashier = actorToken(0, "CASHIER");

        ObjectNode req = invoiceRequest(order1Id, s1);
        JsonNode invoice = ok(HttpMethod.POST, "/api/invoices/generate", cashier, s1, req);
        invoice1Id = invoice.path("id").asLong();
        assertTrue(invoice1Id > 0, "invoice id");

        JsonNode order = ok(HttpMethod.GET, "/api/orders/" + order1Id, cashier, s1, null);
        assertEquals(order1Id.longValue(), invoice.path("orderId").asLong(), "invoice -> order reference");
        assertEquals(order.path("storeId").asLong(), invoice.path("storeId").asLong(), "invoice store = order store");
        assertEquals(order.path("totalAmount").asDouble(), invoice.path("totalAmount").asDouble(), 0.001,
                "invoice total = order total");
        assertEquals(58.0, invoice.path("totalAmount").asDouble(), 0.001);
        assertEquals("GENERATED", invoice.path("status").asText());

        // Invoice lines mirror the order lines (item id, quantity, unit price)
        JsonNode invItems = invoice.path("items");
        assertEquals(order.path("items").size(), invItems.size(), "invoice lines");
        List<String> expectedLines = new java.util.ArrayList<>();
        for (JsonNode it : order.path("items")) {
            expectedLines.add(it.path("itemId").asLong() + "x" + it.path("quantity").asInt()
                    + "@" + String.format("%.2f", it.path("price").asDouble()));
        }
        List<String> actualLines = new java.util.ArrayList<>();
        for (JsonNode it : invItems) {
            actualLines.add(it.path("productId").asLong() + "x" + it.path("quantity").asInt()
                    + "@" + String.format("%.2f", it.path("unitPrice").asDouble()));
        }
        java.util.Collections.sort(expectedLines);
        java.util.Collections.sort(actualLines);
        assertEquals(expectedLines, actualLines, "invoice lines must match order lines");

        // Order <-> invoice relationship, idempotent generation, store listing
        JsonNode byOrder = ok(HttpMethod.GET, "/api/invoices/order/" + order1Id, cashier, s1, null);
        assertEquals(invoice1Id.longValue(), byOrder.path("id").asLong());
        assertEquals(invoice1Id.longValue(),
                ok(HttpMethod.POST, "/api/invoices/generate", cashier, s1, req).path("id").asLong(),
                "generating twice must return the same invoice");
        assertNotNull(findById(asList(ok(HttpMethod.GET, "/api/invoices/store/" + s1, cashier, s1, null)), "id", invoice1Id));

        // A cashier of the other store cannot read this store's invoice
        Long s2 = storeIds.get(1);
        MvcResult foreign = call(HttpMethod.GET, "/api/invoices/" + invoice1Id, actorToken(1, "CASHIER"), s2, null);
        assertRejected(foreign, "cross-store invoice read");
    }

    @Test
    @Order(15)
    void endToEndConsistency() throws Exception {
        assertNotNull(invoice1Id, "invoice must exist");
        String cashier = actorToken(0, "CASHIER");
        Long s1 = storeIds.get(0);
        Long s2 = storeIds.get(1);

        // Business -> master catalog: still exactly the created items
        assertEquals(productIds.size(), asList(getOk("/api/business-product/listByBusiness/" + businessId)).size());
        assertEquals(serviceIds.size(), asList(getOk("/api/business-service/listByBusiness/" + businessId)).size());

        // Master catalog -> store assignment: every store item references a master item exactly once
        for (int s = 0; s < 2; s++) {
            JsonNode products = asList(getOk("/api/store-product/store/" + storeIds.get(s)));
            java.util.Set<Long> seen = new HashSet<>();
            for (JsonNode sp : products) {
                assertTrue(productIds.contains(sp.path("businessProductId").asLong()), "unknown master reference");
                assertTrue(seen.add(sp.path("businessProductId").asLong()), "duplicate store product for a master product");
            }
            assertEquals(productIds.size(), seen.size());
        }

        // Store inventory <-> lots <-> completed orders (store 1)
        Map<Long, JsonNode> stock = inventory(0);
        for (int i = 0; i < 2; i++) {
            Long sp = storeProductIds.get(0).get(i);
            JsonNode row = stock.get(sp);
            assertNotNull(row);
            assertEquals(row.path("totalQuantity").asInt(),
                    row.path("available").asInt() + row.path("blocked").asInt() + row.path("sold").asInt(),
                    "inventory must balance for store product " + sp);
            int lotQty = 0;
            int lotSold = 0;
            for (JsonNode lot : lotsByCode(0, sp).values()) {
                lotQty += lot.path("quantity").asInt();
                lotSold += lot.path("sold").asInt();
                assertEquals(0, lot.path("blocked").asInt(), "no stock left reserved");
            }
            assertEquals(row.path("totalQuantity").asInt(), lotQty, "lots add up to inventory total");
            assertEquals(row.path("sold").asInt(), lotSold, "lot sales add up to inventory sold");
        }
        // Units sold per completed orders: product 1 -> 2, product 2 -> 1 + 4
        assertEquals(2, stock.get(storeProductIds.get(0).get(0)).path("sold").asInt());
        assertEquals(5, stock.get(storeProductIds.get(0).get(1)).path("sold").asInt());

        // Store 2 was never touched by store 1's activity
        Map<Long, JsonNode> stock2 = inventory(1);
        assertStock(stock2, storeProductIds.get(1).get(0), 8, 8, 0, 0);
        assertStock(stock2, storeProductIds.get(1).get(1), 3, 3, 0, 0);
        assertStock(stock2, storeProductIds.get(1).get(2), 4, 4, 0, 0);

        // Orders and invoices agree
        JsonNode second = ok(HttpMethod.POST, "/api/invoices/generate", cashier, s1, invoiceRequest(order2Id, s1));
        assertTrue(second.path("id").asLong() != invoice1Id, "each order has its own invoice");
        assertEquals(32.0, second.path("totalAmount").asDouble(), 0.001);
        assertEquals(2, asList(ok(HttpMethod.GET, "/api/invoices/store/" + s1, cashier, s1, null)).size());
        assertEquals(0, asList(ok(HttpMethod.GET, "/api/invoices/store/" + s2, actorToken(1, "CASHIER"), s2, null)).size());
    }

    // ---------------------------- helpers ----------------------------

    private ObjectNode assignBody(Long catalogItemId, String alias) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("businessId", businessId);
        body.put("catalogItemId", catalogItemId);
        body.putArray("storeIds").add(storeIds.get(0)).add(storeIds.get(1));
        body.put("allStores", false);
        body.put("aliasName", alias);
        body.put("description", "Assigned by test");
        body.put("status", 1);
        return body;
    }

    private ObjectNode invoiceRequest(Long orderId, Long storeId) {
        ObjectNode req = objectMapper.createObjectNode();
        req.put("orderId", orderId);
        req.put("storeId", storeId);
        return req;
    }

    private void addLot(String token, Long storeId, Long storeProductId, String code, int qty, double price,
                        String expiry) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("productId", storeProductId);
        body.put("storeId", storeId);
        body.put("lotCode", code + "-" + suffix);
        body.put("quantity", qty);
        body.put("unitPrice", price);
        body.put("active", 1);
        body.put("purchasedAt", "2026-01-15");
        body.put("mfgDate", "2026-01-01");
        body.put("expiryAt", expiry);
        JsonNode lot = ok(HttpMethod.POST, "/api/product-lots", token, storeId, body);
        assertTrue(lot.path("id").asLong() > 0, "lot id");
        assertEquals(qty, lot.path("quantity").asInt());
        lotIds.put(code, lot.path("id").asLong());
    }

    private Map<String, JsonNode> lotsByCode(int storeIdx, Long storeProductId) throws Exception {
        Long storeId = storeIds.get(storeIdx);
        JsonNode lots = asList(ok(HttpMethod.GET, "/api/product-lots/all/" + storeProductId,
                actorToken(storeIdx, "INVENTORY_MANAGER"), storeId, null));
        Map<String, JsonNode> byCode = new LinkedHashMap<>();
        for (JsonNode lot : lots) {
            String code = lot.path("lotCode").asText();
            byCode.put(code.substring(0, code.length() - suffix.length() - 1), lot);
        }
        return byCode;
    }

    private Map<Long, JsonNode> inventory(int storeIdx) throws Exception {
        Long storeId = storeIds.get(storeIdx);
        JsonNode rows = asList(ok(HttpMethod.GET, "/api/store-inventory/" + storeId,
                actorToken(storeIdx, "INVENTORY_MANAGER"), storeId, null));
        Map<Long, JsonNode> byProduct = new HashMap<>();
        for (JsonNode row : rows) {
            byProduct.put(row.path("productId").asLong(), row);
        }
        return byProduct;
    }

    private void assertStock(Map<Long, JsonNode> stock, Long storeProductId, int total, int available,
                             int blocked, int sold) {
        JsonNode row = stock.get(storeProductId);
        assertNotNull(row, "no inventory row for store product " + storeProductId);
        assertEquals(total, row.path("totalQuantity").asInt(), "total for " + storeProductId);
        assertEquals(available, row.path("available").asInt(), "available for " + storeProductId);
        assertEquals(blocked, row.path("blocked").asInt(), "blocked for " + storeProductId);
        assertEquals(sold, row.path("sold").asInt(), "sold for " + storeProductId);
    }

    private ObjectNode item(long type, Long itemId, int qty, double price, String name) {
        ObjectNode it = objectMapper.createObjectNode();
        it.put("itemType", type);
        it.put("itemId", itemId);
        it.put("quantity", qty);
        it.put("price", price);
        it.put("itemName", name + " " + suffix);
        return it;
    }

    private ObjectNode orderBody(Long storeId, List<ObjectNode> items) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("storeId", storeId);
        body.put("customerId", customerId);
        body.put("notes", "Placed by test");
        body.putArray("items").addAll(items);
        return body;
    }

    private MvcResult placeOrder(String token, Long storeId, List<ObjectNode> items) throws Exception {
        return call(HttpMethod.POST, "/api/orders/new", token, storeId, orderBody(storeId, items));
    }

    private void assertLine(JsonNode items, long type, Long itemId, int qty, double price, String name) {
        for (JsonNode it : items) {
            if (it.path("itemType").asLong() == type && it.path("itemId").asLong() == itemId) {
                assertEquals(qty, it.path("quantity").asInt(), "quantity of " + name);
                assertEquals(price, it.path("price").asDouble(), 0.001, "unit price of " + name);
                assertEquals(name + " " + suffix, it.path("itemName").asText());
                return;
            }
        }
        throw new AssertionError("order line missing: " + name);
    }

    private void assertRejected(MvcResult result, String what) throws Exception {
        int status = result.getResponse().getStatus();
        assertTrue(status >= 400, what + " must be rejected but got " + status + ": "
                + result.getResponse().getContentAsString());
    }

    private String actorToken(int storeIdx, String role) throws Exception {
        String key = storeIdx + ":" + role;
        String token = actorTokens.get(key);
        if (token == null) {
            String email = email("store" + (storeIdx + 1) + "." + role.toLowerCase());
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AssertionError("employee user not found: " + email));
            user.setPassword(passwordEncoder.encode(PASSWORD));
            userRepository.save(user);
            token = login(email);
            assertNotNull(token, "login token for " + email);
            actorTokens.put(key, token);
        }
        return token;
    }

    private MvcResult call(HttpMethod method, String path, String token, Long storeId, ObjectNode body) throws Exception {
        MockHttpServletRequestBuilder rb = request(method, path)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON);
        if (storeId != null) {
            rb.header("X-Store-Id", storeId);
        }
        if (body != null) {
            rb.contentType(MediaType.APPLICATION_JSON).content(body.toString());
        }
        return mockMvc.perform(rb).andReturn();
    }

    private JsonNode ok(HttpMethod method, String path, String token, Long storeId, ObjectNode body) throws Exception {
        MvcResult result = call(method, path, token, storeId, body);
        String content = result.getResponse().getContentAsString();
        int status = result.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, method + " " + path + " -> " + status + " " + content);
        return objectMapper.readTree(content).path("data");
    }

    private static JsonNode asList(JsonNode data) {
        return data.isArray() ? data : data.path("content");
    }

    private static JsonNode findById(JsonNode list, String field, Long id) {
        for (JsonNode n : list) {
            if (n.path(field).asLong() == id) {
                return n;
            }
        }
        return null;
    }

    private ObjectNode employeeBody(String role, String email) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("firstName", role.substring(0, 1) + role.substring(1).toLowerCase().replace('_', ' '));
        body.put("lastName", "Tester");
        body.put("email", email);
        body.put("phone", "+1-555-0300");
        body.put("roleId", roleIds.get(role));
        body.put("jobTitle", role);
        body.put("employeeCode", "EMP-" + suffix + "-" + Math.abs(email.hashCode() % 100000));
        body.put("status", "ACTIVE");
        return body;
    }

    private String email(String local) {
        return local + "." + suffix + "@clapp.test";
    }

    private String login(String email) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("username", email);
        body.put("password", PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(), "login failed: " + result.getResponse().getContentAsString());
        JsonNode token = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("tokenInfo").path("token");
        return token.isMissingNode() ? null : token.asText();
    }

    private JsonNode postOk(String path, ObjectNode body, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertEquals(expectedStatus, result.getResponse().getStatus(), "POST " + path + " -> " + content);
        return objectMapper.readTree(content).path("data");
    }

    private JsonNode getOk(String path) throws Exception {
        MvcResult result = mockMvc.perform(get(path)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertEquals(200, result.getResponse().getStatus(), "GET " + path + " -> " + content);
        return objectMapper.readTree(content).path("data");
    }

    @SafeVarargs
    private static List<String> concat(List<String>... lists) {
        List<String> out = new java.util.ArrayList<>();
        for (List<String> l : lists) out.addAll(l);
        return out;
    }
}
