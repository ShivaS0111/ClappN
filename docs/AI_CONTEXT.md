# 🤖 AI Context Document - CraftLane (ClappN) Platform

**Last Updated**: March 14, 2026  
**Purpose**: Comprehensive context for AI assistants and LLMs  
**Scope**: Complete project understanding for code generation, analysis, and maintenance

---

## Quick Navigation

- [Project Identity](#project-identity)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Core Concepts](#core-concepts)
- [Critical Procedures](#critical-procedures)
- [Code Patterns](#code-patterns)
- [Database Schema](#database-schema-overview)
- [API Patterns](#api-patterns)
- [Security Model](#security-model)
- [Development Standards](#development-standards)
- [Common Tasks](#common-tasks-with-ai)
- [Restrictions & Warnings](#restrictions--warnings)

---

## Project Identity

### What is CraftLane?

**CraftLane (ClappN)** is a complete e-commerce and business management platform that helps small-to-medium businesses and service providers:
- Manage inventory, products, and services
- Process orders both online and offline via POS
- Accept payments through multiple payment gateways
- Run marketing campaigns with offers and coupons
- Track business metrics and analytics
- Manage teams with granular permission controls
- Generate invoices and reports

### Key Characteristics

```
Business Type:      B2B2C SaaS Platform
Market Segment:     SMEs, Retailers, Service Providers
Primary Use:        E-commerce + POS + Inventory Management
Geographic Focus:   India (GST support, INR currency, Razorpay)
Scale:              400+ businesses, 1000+ stores
Transaction Volume: 100k+ orders across platform
```

### Strategic Priorities (2024-2026)

1. **POS Excellence** - Most critical, highest usage
2. **Stability** - Uptime > 99.9%
3. **Performance** - Sub-second API responses
4. **Security** - Multi-level authentication & authorization
5. **Scalability** - Ready for 10x growth

---

## System Architecture

### Overall Structure

```
CraftLane Platform
│
├─ Frontend: React 18 + TypeScript (Vite)
│  ├─ 40+ reusable React components
│  ├─ 18+ feature modules
│  ├─ 20+ custom React hooks (POS module)
│  ├─ Context API for state management
│  └─ Tailwind CSS for styling
│
├─ Backend: Java Spring Boot
│  ├─ 100+ REST API endpoints
│  ├─ Spring Security with JWT auth
│  ├─ Spring Data JPA for ORM
│  ├─ 15+ service classes
│  └─ 30+ database entities
│
└─ Database: MySQL 8.0+
   ├─ 100+ tables
   ├─ Flyway migrations
   ├─ Relational schema
   └─ Optimized indexes

External Integrations:
├─ Razorpay (Payment Gateway - Primary)
├─ Stripe (Payment Gateway - Secondary)
└─ (Email/SMS/Third-party services - Future)
```

### Three-Tier Architecture

```
Presentation Layer (React Frontend)
├─ React Components & Pages
├─ State Management (Context API)
├─ Custom Hooks for logic
└─ UI Components (40+)
            ↓
Business Logic Layer (Spring Boot)
├─ Controllers (REST endpoints)
├─ Services (Business rules)
├─ Repositories (Data access)
└─ Entities (Domain objects)
            ↓
Data Layer (MySQL)
├─ Tables & relationships
├─ Indexes for performance
└─ Constraints & triggers
```

---

## Technology Stack

### Frontend Technologies

```
Core:
├─ React 18.x              (UI framework)
├─ TypeScript              (Type safety - strict mode)
├─ Vite                    (Build tool - blazingly fast)
└─ Node.js 18.x            (Runtime)

Styling:
├─ Tailwind CSS            (Utility-first CSS)
├─ PostCSS                 (CSS processing)
└─ Dark Mode Support       (theme-aware components)

State Management:
├─ React Context API       (Global state)
├─ useReducer              (Complex state logic)
├─ Custom Hooks            (Encapsulated logic)
└─ localStorage            (Persistence)

HTTP & API:
├─ Axios                   (HTTP client)
├─ Custom HTTP wrapper     (Request/response transformation)
└─ JWT token handling      (Authentication)

Routing:
├─ React Router v6         (Client-side routing)
├─ Protected Routes        (Auth guards)
├─ Dynamic Routes          (Role-based routing)
└─ Route permissions       (Access control)

Development:
├─ ESLint                  (Code quality)
├─ Prettier                (Code formatting)
├─ TypeScript Compiler     (Type checking)
└─ Vite Plugins            (Build optimization)
```

### Backend Technologies

```
Core:
├─ Java 17                 (Language)
├─ Spring Boot 2.x         (Framework - embedded Tomcat)
├─ Maven 3.9.x             (Build tool & dependency mgmt)
└─ Gradle                  (Alternative build tool)

Frameworks & Libraries:
├─ Spring MVC              (REST API framework)
├─ Spring Data JPA         (ORM - Hibernate)
├─ Spring Security         (Authentication & authorization)
├─ Spring Validation       (@Valid, custom validators)
├─ Spring AOP              (Aspect-oriented programming)
└─ Log4j / Slf4j           (Logging)

Database:
├─ MySQL 8.0+              (Primary database)
├─ JPA/Hibernate           (ORM layer)
├─ Flyway                  (Database migrations)
├─ HikariCP                (Connection pooling)
└─ JDBC                    (Low-level database access)

Authentication & Security:
├─ JWT (JSON Web Tokens)   (Stateless authentication)
├─ Spring Security         (Auth framework)
├─ BCrypt                  (Password hashing)
├─ CORS                    (Cross-origin requests)
└─ HTTPS/TLS               (Transport security)

External Integrations:
├─ Razorpay SDK            (Payment processing)
├─ Stripe SDK              (Payment processing)
├─ RestTemplate/WebClient  (HTTP clients for APIs)
└─ Jackson                 (JSON serialization)

Development Tools:
├─ Postman                 (API testing collections)
├─ Swagger/OpenAPI         (API documentation)
├─ JUnit 5                 (Unit testing - optional)
└─ Mockito                 (Mocking library - optional)
```

### Database

```
Database: MySQL 8.0+
├─ Character Set: utf8mb4 (full UTF-8 support)
├─ Collation: utf8mb4_unicode_ci
├─ Engine: InnoDB (ACID transactions)
└─ Connection: Port 3306

Migrations: Flyway
├─ versioning/V1__initial.sql
├─ versioning/V2__add_columns.sql
├─ Location: db/migration/
└─ Auto-executed on startup
```

---

## Core Concepts

### 1. Business Model

```
Business (Company registering on platform)
├─ Business Profile
│  ├─ Registration Number
│  ├─ Business Type (12 types)
│  ├─ Owner/Admin
│  └─ Status (ACTIVE/INACTIVE)
│
└─ Stores (Multiple locations per business)
   ├─ Store Details
   │  ├─ Name, Address, Phone
   │  ├─ Operating Hours
   │  └─ Manager/Staff
   │
   └─ Store Operations
      ├─ Products & Inventory
      ├─ Services & Bookings
      ├─ Orders & Sales
      ├─ Payments & Refunds
      └─ Analytics & Reports
```

### 2. Orders & Transactions

```
Order Lifecycle:
PENDING → PROCESSING → COMPLETE → DELIVERED
            ↓
         Can be CANCELLED at any stage

Order Components:
├─ Order Header
│  ├─ Order Number (auto-generated: ORD-20240314-0001)
│  ├─ Customer (optional)
│  ├─ Status
│  ├─ Total Amount
│  ├─ Payment Status
│  └─ Created By (cashier/system)
│
└─ Order Items (1-N relationship)
   ├─ Product or Service
   ├─ Quantity
   ├─ Unit Price
   ├─ Item Total
   ├─ GST on item
   └─ Item-specific notes

Order Totals Calculation:
subtotal = SUM(item_quantity × item_unit_price)
taxableAmount = subtotal - discountAmount
gstAmount = taxableAmount × (gstRate / 100)
totalAmount = taxableAmount + gstAmount
```

### 3. Permissions & Authorization

```
Three-Level Access Control:

Level 1: Roles
ROLE_SYSTEM_ADMIN
├─ All system-level access
└─ Can manage all users

ROLE_BUSINESS_OWNER
├─ Business-level access
└─ Can manage own business

ROLE_STORE_MANAGER
├─ Store-level access
└─ Can manage store operations

ROLE_CASHIER
├─ POS operations
└─ Can create orders only

[15+ roles in total]


Level 2: Feature Permissions
ORDER_CREATE
ORDER_READ
ORDER_UPDATE
ORDER_DELETE
PRODUCT_CREATE
PRODUCT_UPDATE
[100+ permissions]


Level 3: Method-Level Security
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasPermission(#orderId, 'order', 'read')")
@PreAuthorize("hasPermission('product', 'create')")
```

### 4. POS (Point of Sale) System

This is the highest-priority feature. Key aspects:

```
POS Operations:
1. Add items to cart (products or services)
2. Apply discounts
3. Calculate GST
4. Select payment method
5. Process payment
6. Generate invoice
7. Complete order

POS Cart State:
{
  items: [
    { productId, quantity, unitPrice, itemTotal }
  ],
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT',
  discountValue: number,
  discountAmount: calculated,
  gstRate: number,
  gstAmount: calculated,
  grandTotal: calculated,
  paymentMethod: 'CASH' | 'CARD' | 'UPI' | 'WALLET'
}

POS Components:
├─ ProductSelector (product/service browsing)
├─ CartSummary (cart visualization & checkout)
├─ DiscountModal (discount application)
├─ GSTModal (GST calculation & verification)
├─ POSPaymentModal (payment method selection)
└─ OrderConfirmation (order summary & invoice)
```

### 5. Offer & Coupon System

```
Offer (Promotion campaign)
├─ Name & Description
├─ Discount Type (PERCENTAGE or FIXED_AMOUNT)
├─ Discount Value
├─ Validity Period (valid_from, valid_to)
├─ Usage Limit & Tracking
├─ Target Audience (ALL, NEW_CUSTOMERS, VIP)
├─ Min Order Amount
├─ Max Discount Cap
└─ Status (DRAFT, ACTIVE, EXPIRED, ARCHIVED)

Coupon (Promo code)
├─ Coupon Code (e.g., SUMMER20, WELCOME10)
├─ Associated Offer
├─ Usage Limit per code
├─ Usage Limit per customer
├─ Usage Count Tracking
└─ Status (ACTIVE, INACTIVE, EXPIRED)

Application Flow:
1. User enters coupon code at checkout
2. Validate: Not expired, usage limit not exceeded
3. Calculate: discount_amount = order_total × discount%
4. Apply: Reduce order total
5. Track: Increment usage count
```

### 6. Invoice System

```
Invoice Generation:
Order Created → Invoice auto-generated
               ├─ Invoice Number (auto-generated)
               ├─ Invoice Date
               ├─ Business Details integrated
               ├─ Line items from order
               ├─ GST breakdown
               ├─ Payment details
               └─ PDF generation

Invoice Components:
├─ Business Header (name, GSTIN, address, phone)
├─ Invoice Metadata (number, date, time)
├─ Items Table (product/service + qty + amount)
├─ Tax Breakdown (subtotal, GST%, GST amount)
├─ Total Amount with payment method
├─ Customer Details (if applicable)
├─ Notes/Terms
└─ Signature/Authorization mark
```

### 7. Approval Workflow

```
Multi-level Approval Process:

1. Request Created
   ├─ Type: BUSINESS_REGISTRATION, PRODUCT_CREATION, OFFER_APPROVAL, etc.
   ├─ Status: PENDING
   ├─ Routed to: Appropriate reviewer based on type

2. In Review
   └─ Reviewer receives notification
   └─ Views request details
   └─ Adds comments/questions

3. Decision
   ├─ APPROVED → Entity becomes ACTIVE
   ├─ REJECTED → Returned to requester with reason
   └─ Optionally ESCALATED

4. Tracking
   ├─ Approval history maintained
   ├─ Timestamps recorded
   ├─ Activity audited
   └─ Notifications sent
```

---

## Critical Procedures

### Creating New API Endpoint

```java
// 1. Define DTO (src/main/java/biz/craftline/server/dto/)
@Data
@NoArgsConstructor
public class CreateOrderRequest {
    @NotEmpty private List<OrderItem> items;
    private String discountType;
    private Double discountValue;
    private Double gstRate;
}

// 2. Create Controller (src/main/java/biz/craftline/server/controller/)
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    
    @PostMapping
    @PreAuthorize("hasPermission('order', 'create')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(201)
                .body(new ApiResponse(true, 201, "Order created", order));
        } catch (ValidationException e) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, 400, e.getMessage(), null));
        }
    }
}

// 3. Business Logic (Service)
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Validate inventory
        // Calculate totals
        // Create order
        // Generate invoice
        // Return order
    }
}

// 4. Data Access (Repository)
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStoreIdAndStatus(Long storeId, String status);
}
```

### Creating New React Component

```typescript
// 1. Define Types (api-types.ts)
export interface CartItem {
  productId: number;
  quantity: number;
  unitPrice: number;
}

// 2. Create Custom Hook (hooks/usePOSCart.ts)
export function usePOSCart() {
  const [cart, setCart] = useState<CartItem[]>([]);
  
  const addItem = useCallback((item: CartItem) => {
    setCart(prev => [...prev, item]);
  }, []);
  
  return { cart, addItem };
}

// 3. Create Component (components/CartSummary.tsx)
export function CartSummary() {
  const { cart, total } = usePOSCart();
  
  return (
    <div className="cart-summary">
      {/* Component JSX */}
    </div>
  );
}

// 4. Export from Feature Index (index.ts)
export { CartSummary } from './components/CartSummary';
export { usePOSCart } from './hooks/usePOSCart';
```

### Adding New Permission

```java
// 1. Add permission constant (enum or constants)
public enum Permission {
    PRODUCT_CREATE("product.create"),
    ORDER_DELETE("order.delete"),
    NEW_PERMISSION("new.permission")
}

// 2. Assign to roles (in database or code)
INSERT INTO permissions (name, category) 
VALUES ('new.permission', 'Order');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'STORE_MANAGER' AND p.name = 'new.permission';

// 3. Use in code
@PreAuthorize("hasPermission('order', 'delete')")
public void deleteOrder(Long orderId) { }
```

---

## Code Patterns

### Backend Patterns

#### Pattern 1: Service Method with Validation

```java
@Service
public class OrderService {
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 1. Validate
        if (request.getItems().isEmpty()) {
            throw new ValidationException("At least one item required");
        }
        
        // 2. Process
        BigDecimal subtotal = calculateSubtotal(request.getItems());
        BigDecimal discount = calculateDiscount(subtotal, request);
        BigDecimal gst = calculateGST(subtotal, discount, request.getGstRate());
        BigDecimal total = subtotal.subtract(discount).add(gst);
        
        // 3. Persist
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus("PENDING");
        order.setTotalAmount(total);
        
        // 4. Create related entities
        for (OrderItemRequest item : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            order.getItems().add(orderItem);
        }
        
        // 5. Save
        order = orderRepository.save(order);
        
        // 6. Trigger side effects
        invoiceService.generateInvoice(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
        
        return order;
    }
}
```

#### Pattern 2: Permission-based Access

```java
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'order', 'read')")
    public OrderDTO getOrder(@PathVariable Long id) {
        // Only users with 'order.read' permission
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDeleteOrder(#id)")
    public void deleteOrder(@PathVariable Long id) {
        // Custom permission check via method
    }
}
```

### Frontend Patterns

#### Pattern 1: Custom Hook with State Management

```typescript
export function useCreateOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { notify } = useNotification();
  
  const createOrder = useCallback(async (request: CreateOrderRequest) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await httpClient.post('/order', request);
      
      if (response.data.success) {
        notify({
          type: 'success',
          message: 'Order created successfully',
          duration: 3000
        });
        return response.data.data;
      } else {
        throw new Error(response.data.message);
      }
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Failed to create order';
      setError(errMsg);
      notify({
        type: 'error',
        message: errMsg
      });
      return null;
    } finally {
      setLoading(false);
    }
  }, [notify]);
  
  return { createOrder, loading, error };
}
```

#### Pattern 2: Component with Permission Guard

```typescript
export function OrderActions({ orderId }: { orderId: number }) {
  const { can } = useCanAccess();
  const { deleteOrder } = useDeleteOrder();
  
  if (!can('order.delete')) {
    return null; // Don't render if no permission
  }
  
  return (
    <button onClick={() => deleteOrder(orderId)}>
      Delete Order
    </button>
  );
}

// Or using component
<GatedButton permission="order.delete" onClick={() => deleteOrder(orderId)}>
  Delete Order
</GatedButton>
```

#### Pattern 3: Feature Module Structure

```
src/features/pos/
├── pages/
│   └── POSPage.tsx              // Main page
├── components/
│   ├── CartSummary.tsx          // Sub-component
│   ├── PaymentModal.tsx         // Sub-component
│   └── index.ts                 // Barrel export
├── hooks/
│   ├── usePOSCart.ts            // Cart state
│   ├── useCreateOrder.ts        // API call
│   ├── useCalculateGST.ts       // Calculation
│   └── index.ts                 // Barrel export
├── services/
│   ├── posService.ts            // API interface
│   └── calculations.ts          // Business logic
├── api-types.ts                 // TypeScript types
├── api-endpoints.ts             // Endpoint URLs
└── index.ts                     // Module export
```

---

## Database Schema Overview

### Key Entities

```sql
-- Users & Auth (Core)
users(id, email, password_hash, first_name, last_name, status)
roles(id, name, description)
permissions(id, name, category)
user_roles(user_id, role_id)
role_permissions(role_id, permission_id)

-- Business
businesses(id, name, type, registration_number, status, owner_id)
stores(id, business_id, name, address, phone, status)

-- Products & Inventory
categories(id, parent_id, name, description)
products(id, store_id, sku, name, category_id, price, quantity, status)
product_lots(id, product_id, quantity, batch_number, expiry_date)

-- Orders & Transactions
orders(id, store_id, order_number, status, total_amount, payment_status)
order_items(id, order_id, product_id, quantity, unit_price, gst_amount)
invoices(id, order_id, invoice_number, total_amount, pdf_url)
payments(id, order_id, amount, status, gateway, transaction_id)

-- Marketing
offers(id, store_id, name, discount_type, discount_value, valid_to, status)
coupons(id, offer_id, code, usage_count, usage_limit)

-- Workflow
approvals(id, request_type, request_id, status, approver_id, approved_at)

-- Audit
activity_logs(id, user_id, action, entity_type, entity_id, timestamp)
```

### Relationships

```
User (1) ──→ (N) UserRole ──→ (1) Role ──→ (N) RolePermission ──→ (1) Permission
                                                                        
Business (1) ──→ (N) Store ──→ (N) Product ──→ (N) Order ──→ (N) OrderItem
                                      ├─→ (N) Category
                                      └─→ (N) ProductLot
                                      
Store ──→ (N) Service ──→ (N) ServiceBooking
Store ──→ (N) Offer ──→ (N) Coupon ──→ (N) AppliedCoupon
Order ──→ (1) Invoice
Order ──→ (1) Payment
```

---

## API Patterns

### Request/Response Format

```json
// Success Response
{
  "success": true,
  "statusCode": 200,
  "message": "Operation successful",
  "timestamp": "2024-03-14T10:30:00Z",
  "data": { /* actual data */ }
}

// Error Response
{
  "success": false,
  "statusCode": 400,
  "message": "Validation error",
  "errors": [
    { "field": "email", "message": "Invalid email" }
  ]
}
```

### Pagination

```
GET /api/order?page=1&pageSize=20&sort=createdAt,desc

Response:
{
  "content": [ /* items */ ],
  "totalElements": 500,
  "totalPages": 25,
  "currentPage": 1,
  "pageSize": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

### Filtering

```
GET /api/order?status=PENDING&storeId=456&createdAfter=2024-03-01
GET /api/product?categoryId=10&minPrice=100&maxPrice=5000&search=laptop
```

---

## Security Model

### JWT Token Structure

```
Header: { "alg": "HS256", "typ": "JWT" }

Payload: {
  "sub": "user@example.com",
  "userId": 1,
  "role": "STORE_MANAGER",
  "permissions": ["product.create", "order.read"],
  "businessId": 123,
  "storeId": 456,
  "iat": 1710425400,
  "exp": 1710511800
}

Signature: HMACSHA256(base64(header) + "." + base64(payload), SECRET_KEY)
```

### Permission Checking Process

```
1. Request arrives
   ↓
2. JwtFilter extracts token
   ↓
3. Token validated & user loaded
   ↓
4. User permissions loaded into SecurityContext
   ↓
5. @PreAuthorize expressions evaluated
   ↓
6. Method executed or forbidden
```

---

## Development Standards

### Code Style Guidelines

#### Backend (Java)
```java
// Class naming: PascalCase
public class OrderService { }

// Method naming: camelCase
public Order createOrder() { }

// Constant naming: UPPER_SNAKE_CASE
public static final String ORDER_STATUS_PENDING = "PENDING";

// Packages: com.company.product.domain
com.craftline.server.service
com.craftline.server.repository
com.craftline.server.controller

// Documentation
/**
 * Creates a new order with provided items.
 *
 * @param request The order creation request with items and discount
 * @return The created Order entity with auto-generated ID
 * @throws ValidationException if items list is empty or invalid
 * @throws InventoryException if items not in stock
 */
```

#### Frontend (TypeScript/React)
```typescript
// Component naming: PascalCase
export function CartSummary() { }

// Hook naming: useCamelCase
export function usePOSCart() { }

// Function naming: camelCase
const calculateGST = () => { }

// Constant naming: UPPER_SNAKE_CASE
export const MIN_ORDER_AMOUNT = 100;

// Type naming: PascalCase
interface OrderRequest { }
type OrderStatus = 'PENDING' | 'COMPLETE';

// File structure: features/[feature]/[type]/
src/features/pos/components/CartSummary.tsx
src/features/pos/hooks/usePOSCart.ts
src/features/pos/services/posService.ts
```

### Commit Message Standards

```
Format: <type>: <description>

Types:
feat:     New feature
fix:      Bug fix
refactor: Code refactoring
perf:     Performance improvement
test:     Test additions
docs:     Documentation
chore:    Dependencies, build, etc.

Examples:
feat: implement POS cart calculation with GST
fix: correct discount percentage calculation
refactor: extract cart logic into custom hook
perf: optimize order list pagination query
docs: add API endpoint documentation
```

### Testing Standards

```
Backend:
- Unit test for services
- Integration test for APIs
- Test coverage: > 70%
- Pattern: Given-When-Then

Frontend:
- Component tests (React Testing Library)
- Hook tests
- Integration tests
- Test coverage: > 60%
```

---

## Common Tasks with AI

### Task Type 1: Create New API Endpoint

```
User Request: "Create an endpoint to export orders as CSV"

AI Should:
1. Define DTO for request/response
2. Create or update Controller method
3. Implement Service logic
4. Add permission check (@PreAuthorize)
5. Return proper ResponseEntity
6. Handle exceptions
7. Document with JavaDoc
```

### Task Type 2: Add New React Component

```
User Request: "Create a component to display order summary"

AI Should:
1. Create component file in proper location
2. Define TypeScript interfaces
3. Add JSDoc comments
4. Use existing hooks if applicable
5. Follow Tailwind CSS patterns
6. Add error handling
7. Export from barrel index
```

### Task Type 3: Add New Permission

```
User Request: "Add permission for refund processing"

AI Should:
1. Define permission constant/name
2. Add to database migration
3. Add permission guard to API endpoint
4. Add frontend permission check
5. Document in permission system
```

### Task Type 4: Fix Bug

```
User Request: "GST calculation showing wrong value in orders"

AI Should:
1. Identify calculation logic (frontend or backend)
2. Check formula: taxable × (rate / 100)
3. Verify precision (use BigDecimal in backend)
4. Check rounding issues
5. Add test case
6. Verify fix works end-to-end
```

### Task Type 5: Performance Optimization

```
User Request: "Order list page loading slowly"

AI Should:
1. Check if query issues (N+1 problem)
2. Suggest pagination
3. Check index usage
4. Consider caching
5. Profile response time
6. Recommend optimization
```

---

## Restrictions & Warnings

### ⚠️ DO NOT

```
❌ DO NOT modify existing database columns without migration
   → Always create new migration file in db/migration/

❌ DO NOT skip type checking in TypeScript
   → Use strict mode: tsconfig.json strict: true

❌ DO NOT commit sensitive data
   → Never commit .env file with secrets
   → Never commit API keys or passwords

❌ DO NOT bypass authentication/authorization
   → Always use @PreAuthorize for APIs
   → Always check permissions in frontend

❌ DO NOT modify permission system without governance approval
   → Document all permission changes
   → Test thoroughly before deployment

❌ DO NOT use any external payment gateway without integration
   → Only use Razorpay/Stripe implemented gates
   → Never hardcode payment credentials

❌ DO NOT change order calculation logic without testing
   → Always verify GST, discounts, totals
   → Test with multiple scenarios

❌ DO NOT make breaking API changes without versioning
   → Use /api/v1/, /api/v2/ for versions
   → Maintain backward compatibility
```

### ✅ DO

```
✅ Always include proper error handling with try-catch
✅ Always validate input on both frontend and backend
✅ Always write TypeScript types (no 'any' type)
✅ Always document complex logic with comments
✅ Always test changes locally before committing
✅ Always follow existing code patterns
✅ Always use proper HTTP status codes
✅ Always return consistent response format
✅ Always log important operations
✅ Always commit frequently with clear messages
```

### 🚨 Critical System Areas

```
CRITICAL - Never modify without review:
├── Authentication & JWT handling
├── Payment processing
├── Database migrations
├── Order calculation logic
├── Permission system
├── Invoice generation
└── GST calculation

HIGH PRIORITY - Test thoroughly:
├── Order creation
├── Discount application
├── Payment status updates
├── Inventory updates
└── Invoice generation
```

---

## Quick Reference Sheets

### Common Backend Operations

```java
// Create entity
Order order = new Order();
order.setOrderNumber("ORD-" + System.currentTimeMillis());
order = orderRepository.save(order);

// Find entity
Order order = orderRepository.findById(123).orElseThrow();

// Update entity
order.setStatus("COMPLETE");
orderRepository.save(order);

// Delete entity
orderRepository.deleteById(123);

// Query with conditions
List<Order> orders = orderRepository.findByStoreIdAndStatus(456, "PENDING");

// Throw validation exception
throw new ValidationException("Order amount must be positive");

// Return success response
return ResponseEntity.ok(new ApiResponse(true, 200, "Success", data));

// Return error response
return ResponseEntity.badRequest()
  .body(new ApiResponse(false, 400, "Error message", null));
```

### Common Frontend Operations

```typescript
// Custom hook for API call
const { data, loading, error } = useGetOrders(storeId);

// Component with state
const [cart, setCart] = useState<CartItem[]>([]);

// Update cart
setCart(prev => [...prev, newItem]);

// Call API endpoint
await httpClient.post('/order', orderData);

// Check permission
const { can } = useCanAccess();
if (can('order.delete')) { /* show delete button */ }

// Show notification
const { notify } = useNotification();
notify({ type: 'success', message: 'Order created!' });

// Use effect for side effects
useEffect(() => {
  fetchOrders();
}, [storeId]); // Dependency array

// Conditional rendering
{isLoading ? <Loader /> : <Content />}

// List rendering
{items.map(item => <Item key={item.id} {...item} />)}

// Calculate total
const total = items.reduce((sum, item) => sum + item.price, 0);
```

---

## Resources & References

### Key Files to Read
- Frontend: `src/features/pos/Documentation/QUICK_REFERENCE.md`
- Backend: `ClappN/src/main/resources/` configuration files
- Database: `db/startup.sql` for schema reference
- API: `ClappN/postman/` for endpoint examples

### Important Directories

```
Frontend:
├── src/features/ (18+ feature modules)
├── src/components/ (40+ reusable components)
├── src/common/ (shared utilities)
└── src/routes/ (routing configuration)

Backend:
├── src/main/java/biz/craftline/server/
├── src/main/resources/ (configurations)
├── db/migration/ (database migrations)
└── pom.xml (dependencies)
```

### Tools & Services
- Postman: API testing collections in `ClappN/postman/`
- Swagger: API docs at `http://localhost:8080/swagger-ui.html`
- MySQL Workbench: Database management
- GitHub: Version control

---

## Getting Help

When asking for AI assistance:

✅ **DO provide**:
- Clear description of what needs to be done
- Current code or error message
- Which feature/file is affected
- Expected outcome

❌ **DON'T provide**:
- Entire codebase (use specific file references)
- Credentials or secrets
- Confidential information

---

## Summary

**CraftLane (ClappN)** is a sophisticated e-commerce platform with:
- ✅ 100+ API endpoints
- ✅ 40+ React components
- ✅ 100+ database tables
- ✅ 100+ permissions
- ✅ Multi-layer security
- ✅ Production-ready code

Use this document as your guide when working on any part of the system.

---

**Last Updated**: March 14, 2026  
**Document Version**: 2.0  
**Status**: ✅ Comprehensive & Current

**For questions contact**: Development Team  
**Repository**: GitHub / GitLab
