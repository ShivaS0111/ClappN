# 🏗️ CraftLane Architecture Guide

**Last Updated**: March 14, 2026  
**Scope**: Complete backend & frontend architecture  
**Level**: Advanced developers

---

## Table of Contents
1. [System Architecture Overview](#system-architecture-overview)
2. [Backend Architecture](#backend-architecture)
3. [Frontend Architecture](#frontend-architecture)
4. [Database Design](#database-design)
5. [API Design](#api-design)
6. [Authentication & Authorization](#authentication--authorization)
7. [Data Flow Patterns](#data-flow-patterns)
8. [Deployment Architecture](#deployment-architecture)

---

## System Architecture Overview

### High-Level System Design

```
┌─────────────────────────────────────────────────────────────────┐
│                          End Users                               │
│  (Business Owners, Customers, Cashiers, Approvers, etc.)       │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTPS / TLS
                         ▼
        ┌────────────────────────────────────┐
        │     Frontend (React + TypeScript)   │
        │  • Component Library (40+)          │
        │  • State Management (Context API)   │
        │  • Custom Hooks (20+)               │
        │  • Responsive Design (Tailwind)     │
        │  • Dark Mode Support                │
        │  • 18+ Feature Modules              │
        └────────────┬─────────────────────────┘
                     │ REST API / JSON
                     ▼
        ┌────────────────────────────────────┐
        │   Backend (Java Spring Boot)        │
        │  • Spring MVC / REST APIs           │
        │  • Spring Data JPA                  │
        │  • Spring Security + JWT            │
        │  • 100+ API Endpoints               │
        │  • RBAC & Permission System         │
        │  • Business Logic Tier              │
        └────────────┬─────────────────────────┘
                     │ JDBC
                     ▼
        ┌────────────────────────────────────┐
        │    MySQL Database (8.0+)            │
        │  • 100+ Tables                      │
        │  • Relational Schema                │
        │  • Flyway Migrations                │
        │  • Indexed for Performance          │
        └────────────────────────────────────┘
                     │
    ┌────────────────┼────────────────┐
    ▼                ▼                ▼
  Razorpay        Stripe         External
  Payment      Payment        Services
  Gateway      Gateway        (Future)
```

### Component Interaction

```
User Request
    ↓
[React Component] ← Display logic
    ↓
[Custom Hook] ← Business logic, validation
    ↓
[HTTP Client] ← Transform & send request
    ↓
[Spring Controller] ← Route & parse
    ↓
[Service Layer] ← Business rules
    ↓
[Data Access Layer] ← Database operations
    ↓
[MySQL Database] ← Persistence
    ↓
[Response] ← All way back
    ↓
[React Component] ← Display result
```

---

## Backend Architecture

### Layered Architecture

```
Backend (Java Spring Boot)
│
├─ Controller Layer (REST Endpoints)
│  │
│  ├── /api/order/*              (Order operations)
│  ├── /api/invoice/*            (Invoice generation)
│  ├── /api/payment/*            (Payment processing)
│  ├── /api/product/*            (Catalog management)
│  ├── /api/service/*            (Service management)
│  ├── /api/offer/*              (Offer management)
│  ├── /api/approval/*           (Approval workflow)
│  ├── /api/business/*           (Business management)
│  ├── /api/store/*              (Store operations)
│  ├── /api/user/*               (User management)
│  ├── /api/permission/*         (Permission management)
│  └── [10+ more controllers]
│
├─ Service Layer (Business Logic)
│  │
│  ├── OrderService              (Order creation, updates)
│  ├── InvoiceService            (Invoice generation)
│  ├── PaymentService            (Payment processing)
│  ├── ProductService            (Product catalog)
│  ├── OfferService              (Discount logic)
│  ├── ApprovalService           (Workflow engine)
│  ├── AuthenticationService     (Auth logic)
│  ├── PermissionService         (RBAC)
│  └── [15+ more services]
│
├─ Repository Layer (Data Access)
│  │
│  ├── OrderRepository           (JPA Interface)
│  ├── InvoiceRepository         (JPA Interface)
│  ├── ProductRepository         (JPA Interface)
│  ├── UserRepository            (JPA Interface)
│  └── [30+ more repositories]
│
├─ Entity Layer (Domain Objects)
│  │
│  ├── Order, OrderItem
│  ├── Invoice, InvoiceItem
│  ├── Product, ProductLot
│  ├── Service, ServiceBooking
│  ├── User, Role, Permission
│  ├── Business, Store, Vendor
│  ├── Offer, Coupon, Approval
│  └── [30+ more entities]
│
├─ Utility Layer (Cross-cutting concerns)
│  │
│  ├── JWT Token Manager         (Auth tokens)
│  ├── Status Enums              (State constants)
│  ├── Exception Handlers        (Error handling)
│  ├── Validators                (Input validation)
│  ├── Constants                 (App-wide constants)
│  └── Utils                     (Helper functions)
│
└─ Configuration Layer
   │
   ├── SecurityConfig            (Spring Security setup)
   ├── DatabaseConfig            (JPA configuration)
   ├── CorsConfig                (CORS settings)
   ├── SwaggerConfig             (API documentation)
   └── ApplicationProperties     (Environment config)
```

### Spring Boot Features Used

| Feature | Purpose | Implementation |
|---------|---------|-----------------|
| **Spring MVC** | REST API framework | `@RestController`, `@RequestMapping` |
| **Spring Data JPA** | ORM & data persistence | `JpaRepository`, `@Entity`, `@Column` |
| **Spring Security** | Authentication & auth | `SecurityConfig`, `JwtFilter`, `@PreAuthorize` |
| **Spring Validation** | Input validation | `@Valid`, `@Constraint` |
| **Aspect-Oriented Programming** | Cross-cutting concerns | Logging, transaction management |
| **Dependency Injection** | Loose coupling | `@Autowired`, `@Component`, `@Service` |
| **Exception Handling** | Unified error responses | `@ExceptionHandler`, custom exceptions |

### Key Service Methods Pattern

```java
// OrderService example
@Service
public class OrderService {
    
    // Create order
    public Order createOrder(CreateOrderRequest request) {
        // 1. Validate request & inventory
        // 2. Calculate totals & apply discounts
        // 3. Calculate & add GST
        // 4. Create order record
        // 5. Create order items
        // 6. Reserve inventory
        // 7. Trigger invoice generation
        // 8. Return order
    }
    
    // Update status
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        // 1. Validate state transition
        // 2. Update order
        // 3. Handle inventory (if cancelled)
        // 4. Notify stakeholders
        // 5. Log activity
    }
    
    // Get with details
    public OrderDetailsDTO getOrderDetails(Long orderId) {
        // 1. Fetch order
        // 2. Check permissions
        // 3. Get items, invoice, payment details
        // 4. Calculate summaries
        // 5. Return DTO
    }
}
```

---

## Frontend Architecture

### Feature-Based Module Structure

```
src/features/
├── pos/ (Point of Sale)
│   ├── pages/
│   │   └── POSPage.tsx
│   ├── components/
│   │   ├── CartSummary.tsx
│   │   ├── DiscountModal.tsx
│   │   ├── GSTModal.tsx
│   │   ├── POSPaymentModal.tsx
│   │   └── ProductSelector.tsx
│   ├── hooks/
│   │   ├── usePOS.ts (cart state)
│   │   ├── useCreateOrder.ts (backend call)
│   │   ├── useCalculateGST.ts (calculations)
│   │   ├── useApplyDiscount.ts (discount logic)
│   │   └── [15+ more hooks]
│   ├── services/
│   │   └── posService.ts (API calls & methods)
│   ├── api-types.ts (TypeScript DTOs)
│   ├── api-endpoints.ts (Endpoint constants)
│   └── Documentation/
│       ├── QUICK_REFERENCE.md
│       ├── IMPLEMENTATION_GUIDE.md
│       └── API_INTEGRATION_ANALYSIS.md
│
├── auth/ (Authentication)
│   ├── AuthProvider.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   └── useCanAccess.ts (permissions)
│   └── services/
│       └── authServices.ts
│
├── dashboard/ (Admin Dashboards)
│   ├── pages/
│   │   ├── DashboardPage.tsx
│   │   ├── SystemAdminDashboard.tsx
│   │   └── BusinessOwnerDashboard.tsx
│   ├── components/
│   │   ├── SalesChart.tsx
│   │   ├── KPICards.tsx
│   │   └── ActivityFeed.tsx
│   └── services/
│       └── dashboardService.ts
│
├── offers/ (Marketing)
│   ├── pages/
│   │   └── OfferManagementPage.tsx
│   ├── components/
│   │   ├── OfferForm.tsx
│   │   ├── OffersList.tsx
│   │   └── OfferStats.tsx
│   └── services/
│       └── offerService.ts
│
├── approvals/ (Workflow)
│   ├── pages/
│   │   └── ApprovalRequestsPage.tsx
│   ├── hooks/
│   │   └── useApprovals.ts
│   └── services/
│       └── approvalService.ts
│
└── [12+ more feature modules]
    ├── business/
    ├── products/
    ├── categories/
    ├── services/
    ├── store/
    ├── vendor/
    ├── reports/
    ├── profile/
    ├── permissions/
    ├── coupons/
    ├── brands/
    └── userManagement/
```

### Component Hierarchy Pattern

```
Page Component (POSPage)
├── Layout Wrapper
│   ├── Header (with search & user menu)
│   ├── Main Content Area
│   │   ├── ProductSelector
│   │   │   ├── CategoryFilter
│   │   │   ├── SearchBar
│   │   │   └── ProductGrid
│   │   │       └── ProductCard (x N)
│   │   │
│   │   └── CartSummary
│   │       ├── CartItemsList
│   │       │   └── CartItem (x N)
│   │       ├── DiscountSection
│   │       ├── GSTCalculation
│   │       ├── TotalAmount
│   │       └── ProceedButton
│   │
│   └── Modals
│       ├── DiscountModal
│       │   ├── DiscountTypeSelector
│       │   ├── AmountInput
│       │   └── ApplyButton
│       ├── GSTModal
│       │   ├── GSTRateSelector
│       │   └── CalculationDisplay
│       └── POSPaymentModal
│           ├── PaymentMethodSelector
│           └── ConfirmButton
│
└── Notifications (Toast)
    └── NotificationProvider
```

### State Management Flow

```
User Action (e.g., Add to Cart)
    ↓
React Component Handler
    ↓
Update Context via useReducer
    ↓
Local State Updated
    ↓
Component Re-renders with new state
    ↓
API Call via Custom Hook (useCreateOrder)
    ↓
HTTP Request to Backend
    ↓
Backend Response
    ↓
Custom Hook processes response
    ↓
Update Context with server data
    ↓
Component displays updated data
    ↓
Show Toast notification
```

### Custom Hook Pattern

```typescript
// Hook structure example
export function usePOSCart() {
  const [cart, dispatch] = useReducer(cartReducer, initialState);
  const { notify } = useNotification();
  
  // Actions
  const addItem = useCallback((product) => {
    dispatch({ type: 'ADD_ITEM', payload: product });
    notify({ message: 'Item added', type: 'success' });
  }, [dispatch, notify]);
  
  const removeItem = useCallback((productId) => {
    dispatch({ type: 'REMOVE_ITEM', payload: productId });
  }, [dispatch]);
  
  const updateQuantity = useCallback((productId, quantity) => {
    dispatch({ type: 'UPDATE_QUANTITY', payload: { productId, quantity } });
  }, [dispatch]);
  
  // Selectors
  const total = useMemo(() => 
    cart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0),
    [cart.items]
  );
  
  return { cart, total, addItem, removeItem, updateQuantity };
}
```

---

## Database Design

### Entity Relationship Diagram (Simplified)

```
Users (1) ──────── (N) UserRoles
  │
  ├── (1) ────── (N) Businesses
  │                        │
  │                        └── (1) ────── (N) Stores
  │                                           │
  │                                           ├── (1) ────── (N) Orders
  │                                           │                 │
  │                                           │                 ├── (N) OrderItems
  │                                           │                 │       └── (N) Products
  │                                           │                 │
  │                                           │                 ├── (1) ────── (1) Invoice
  │                                           │                 │
  │                                           │                 └── (1) ────── (1) Payment
  │                                           │
  │                                           ├── (1) ────── (N) Products
  │                                           │       │
  │                                           │       ├── (N) Categories
  │                                           │       │
  │                                           │       └── (N) ProductLots
  │                                           │
  │                                           ├── (1) ────── (N) Services
  │                                           │
  │                                           └── (1) ────── (N) StoreOffers
  │
  ├── (1) ────── (N) Approvals
  │
  └── (1) ────── (N) Permissions
        (through UserRoles)
```

### Key Entities

```
Users
├── id (PK)
├── email, password_hash
├── first_name, last_name
├── phone, avatar_url
├── created_at, updated_at
└── status

Orders
├── id (PK)
├── store_id (FK)
├── order_number (UNIQUE)
├── customer_id (FK, nullable)
├── status (ENUM)
├── total_amount, discount_amount, gst_amount
├── payment_status
├── created_at, updated_at
└── created_by (FK)

OrderItems
├── id (PK)
├── order_id (FK)
├── product_id / service_id (FK)
├── quantity, unit_price
├── item_total, gst_amount
└── created_at

Invoices
├── id (PK)
├── order_id (FK, UNIQUE)
├── invoice_number (UNIQUE)
├── invoice_date
├── items (JSON or related table)
├── subtotal, tax_amount, total_amount
├── payment_method
└── file_url (PDF generated)

Products
├── id (PK)
├── store_id (FK)
├── sku, name, description
├── category_id (FK)
├── price, cost_price
├── inventory_quantity
├── status
└── created_at, updated_at

Services
├── id (PK)
├── store_id (FK)
├── name, description
├── price, duration_minutes
├── availability_status
└── rating, review_count

Offers/Coupons
├── id (PK)
├── store_id (FK)
├── code, name
├── discount_type (ENUM: PERCENTAGE, FIXED_AMOUNT)
├── discount_value
├── min_order_amount (nullable)
├── max_discount_cap (nullable)
├── valid_from, valid_to
├── usage_limit, usage_count
├── status
└── created_at

Approvals
├── id (PK)
├── request_type (ENUM)
├── request_id (polymorphic FK)
├── requester_id (FK)
├── approver_id (FK)
├── status (ENUM: PENDING, APPROVED, REJECTED)
├── comments
├── created_at, updated_at, approved_at
└── rejection_reason (nullable)

Permissions
├── id (PK)
├── name (UNIQUE)
├── description
├── category
├── status
└── created_at

Roles
├── id (PK)
├── name (UNIQUE)
├── description
├── status
└── created_at

RolePermissions
├── role_id (FK)
├── permission_id (FK)
└── created_at
```

### Indexing Strategy

```sql
-- Performance Indexes
CREATE INDEX idx_orders_store_id ON orders(store_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_products_store_id ON products(store_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_approvals_status ON approvals(status);
CREATE INDEX idx_offers_valid_to ON offers(valid_to);
-- ... more indexes for frequently queried fields
```

---

## API Design

### RESTful Endpoint Patterns

```
Orders:
GET    /api/order                 (List all orders)
POST   /api/order                 (Create new order)
GET    /api/order/{id}            (Get order details)
PUT    /api/order/{id}            (Update order)
PUT    /api/order/{id}/status     (Update status)
DELETE /api/order/{id}            (Cancel order)

Invoices:
GET    /api/invoice               (List invoices)
POST   /api/invoice/generate      (Generate from order)
GET    /api/invoice/{id}          (Get invoice details)
GET    /api/invoice/{id}/pdf      (Download PDF)

Payments:
POST   /api/payment/initiate      (Start payment process)
GET    /api/payment/{id}          (Get payment status)
POST   /api/payment/webhook       (Payment gateway callback)

Products:
GET    /api/product               (List products)
POST   /api/product               (Create product)
GET    /api/product/{id}          (Get product details)
PUT    /api/product/{id}          (Update product)
DELETE /api/product/{id}          (Delete product)
GET    /api/product/search        (Search products)

Offers:
GET    /api/offer                 (List offers)
POST   /api/offer                 (Create offer)
GET    /api/offer/{id}            (Get offer details)
PUT    /api/offer/{id}            (Update offer)
DELETE /api/offer/{id}            (Delete offer)

Approvals:
GET    /api/approval              (List pending approvals)
POST   /api/approval/{id}/approve (Approve request)
POST   /api/approval/{id}/reject  (Reject request)
GET    /api/approval/{id}         (Get approval details)

... (100+ more endpoints)
```

### Standard Response Format

```json
// Success Response
{
  "success": true,
  "statusCode": 200,
  "message": "Order created successfully",
  "timestamp": "2024-03-14T10:30:00Z",
  "data": {
    "id": "ORD-123456",
    "total_amount": 5000,
    ...
  }
}

// Error Response
{
  "success": false,
  "statusCode": 400,
  "message": "Validation error",
  "timestamp": "2024-03-14T10:30:00Z",
  "errors": [
    {
      "field": "quantity",
      "message": "Quantity must be positive"
    }
  ]
}
```

### Request/Response DTOs

```typescript
// Frontend Types (API_TYPES.TS)
interface CreateOrderRequest {
  items: OrderItem[];
  discount_type?: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discount_value?: number;
  gst_rate: number;
  payment_method: 'CASH' | 'CARD' | 'UPI' | 'OTHER';
  payment_gateway?: 'RAZORPAY' | 'STRIPE';
}

interface OrderResponse {
  id: string;
  order_number: string;
  items: OrderItemResponse[];
  subtotal: number;
  discount_amount: number;
  gst_amount: number;
  total_amount: number;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETE' | 'CANCELLED';
  payment_status: 'PENDING' | 'PAID' | 'FAILED';
  created_at: string;
}

interface OrderItem {
  product_id?: string;
  service_id?: string;
  quantity: number;
  unit_price: number;
}
```

---

## Authentication & Authorization

### JWT Token Flow

```
1. User Login
   └─→ /api/auth/login (POST)
       ├─ Credentials: email + password
       ├─ Backend validates in database
       └─ Returns JWT token + refresh token

2. Token Structure
   Header:
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   
   Payload:
   {
     "sub": "user@example.com",
     "role": "STORE_MANAGER",
     "permissions": ["product.create", "order.read"],
     "business_id": 123,
     "store_id": 456,
     "iat": 1710425400,
     "exp": 1710511800  // 1 day expiry
   }
   
   Signature:
   HMACSHA256(base64(header) + "." + base64(payload), SECRET_KEY)

3. Token Usage
   ├─ Client stores in localStorage
   ├─ Includes in Authorization header
   │  └─ Authorization: Bearer <jwt_token>
   ├─ Backend intercepts & validates
   └─ Extracts user/permissions from token

4. Token Refresh
   └─ When token nears expiry
       └─ POST /api/auth/refresh
           └─ Returns new access token

5. Token Logout
   └─ Clear token from client
   └─ Optional: Blacklist token on backend
```

### Permission System (RBAC)

```
Three-Level Access Control:

Level 1: Role-Based
├── SystemAdmin        (All permissions)
├── BusinessOwner      (Business-level permissions)
├── StoreManager       (Store-level permissions)
├── Cashier            (POS operations only)
└── [12+ more roles]

Level 2: Feature-Based
├── ORDER_CREATE
├── ORDER_UPDATE
├── ORDER_DELETE
├── PRODUCT_CREATE
├── PRODUCT_UPDATE
├── [100+ features]

Level 3: Permission-Based (Granular)
├── product.create
├── product.update
├── product.delete
├── order.process_payment
├── approval.approve
├── inventory.view
├── inventory.update
└── [100+ granular permissions]
```

### Authorization Guards

```
Backend (Spring Security):
├── @PreAuthorize("hasRole('ADMIN')")
├── @PreAuthorize("hasPermission(#orderId, 'order', 'read')")
├── @PreAuthorize("@permissionService.canAccessOrder(#orderId)")
└── Method-level security annotations

Frontend (React):
├── useCanAccess() hook
├── PermissionGuard component
├── GatedButton component
├── GatedLink component
├── ProtectedRoute component
└── Feature-based controls
```

---

## Data Flow Patterns

### Create Order Flow (Detailed)

```
Browser (React)
    ↓
User clicks "Create Order"
    ↓
POSPage component calls useCreateOrder()
    ↓
Custom hook validates:
  ├─ Cart not empty ✓
  ├─ Items in stock ✓
  ├─ Discount valid ✓
  └─ GST rate valid ✓
    ↓
Constructs CreateOrderRequest:
{
  "items": [
    {"product_id": 1, "quantity": 2, "unit_price": 500}
  ],
  "discount_type": "PERCENTAGE",
  "discount_value": 10,
  "gst_rate": 18,
  "payment_method": "UPI"
}
    ↓
HTTP Client intercepts:
  ├─ Adds JWT token to Authorization header
  ├─ Transforms request data
  └─ Sends POST request to /api/order
    ↓
Spring Boot Backend (OrderController)
    ↓
JwtFilter:
  ├─ Extracts & validates JWT token
  ├─ Loads user & permissions
  └─ Stores in SecurityContext
    ↓
OrderController.createOrder():
  ├─ @PreAuthorize("hasPermission('order', 'create')")
  ├─ @Valid validates request
  └─ Calls OrderService
    ↓
OrderService.createOrder():
  ├─ Load store (permission check)
  ├─ Validate inventory for each item
  ├─ Calculate:
  │  ├─ Item subtotal = quantity × unit_price
  │  ├─ Total = sum of items
  │  ├─ Discount = (total × discount% OR fixed_amount)
  │  ├─ Taxable = total - discount
  │  └─ GST = taxable × gst_rate%
  ├─ Create order entity
  ├─ Create order items
  ├─ Reserve inventory
  ├─ Publish event: OrderCreatedEvent
  └─ Transaction commit
    ↓
InvoiceService (listener):
  ├─ Generate invoice from order
  ├─ Format & save
  └─ Make available for download
    ↓
OrderRepository.save():
  ├─ SQL INSERT into orders table
  ├─ SQL INSERT into order_items table
  ├─ Return saved entity with ID
    ↓
OrderController returns:
{
  "success": true,
  "statusCode": 201,
  "data": {
    "id": 456,
    "order_number": "ORD-20240314-0001",
    "total_amount": 1242,
    "gst_amount": 180,
    ...
  }
}
    ↓
HTTP Response received by client
    ↓
useCreateOrder hook:
  ├─ Checks success flag
  ├─ Saves order to Context
  ├─ Clears cart
  └─ Returns response
    ↓
React component:
  ├─ Updates UI
  ├─ Triggers toast: "Order created!"
  └─ Redirects to order details page
    ↓
User sees: Order confirmation with invoice link
```

### Discount Application Flow

```
User selects discount type in DiscountModal
    ↓
Frontend calls useApplyDiscount(type, value):
├─ Type: 'PERCENTAGE' or 'FIXED_AMOUNT'
├─ Value: discount amount
    ↓
Validation:
├─ If PERCENTAGE:
│  └─ Ensure 0 < value <= 100
├─ If FIXED_AMOUNT:
│  ├─ Ensure value > 0
│  └─ Ensure value < cart_total
    ↓
Calculation using useMemo:
├─ discountAmount = 
│   type === 'PERCENTAGE' 
│     ? (cartTotal × value / 100)
│     : value
├─ taxableAmount = cartTotal - discountAmount
└─ gstAmount = taxableAmount × gstRate / 100
    ↓
Update cartContext:
├─ discount_type = type
├─ discount_value = value
├─ discount_amount = discountAmount
├─ gst_amount = gstAmount
├─ grand_total = taxableAmount + gstAmount
    ↓
Component re-renders with:
├─ Updated price display
├─ Total breakdown
└─ Apply/Cancel buttons
```

---

## Deployment Architecture

### Development Environment
```
Developer Laptop
├── Frontend (ng serve)
│  └── http://localhost:5173
├── Backend (mvn spring-boot:run)
│  └── http://localhost:8080
├── MySQL (local or Docker)
│  └── localhost:3306
└── Mock data for development
```

### Production Environment (Proposed)
```
Load Balancer (Nginx)
    ↓
┌─────────────────────────┐
├─ Frontend (React build) │  Multiple instances
│  └─ Static files        │
├─ CDN caching           │
└─────────────────────────┘
    ↓
┌─────────────────────────┐
├─ API Gateway          │
├─ Rate limiting        │
├─ Request routing      │
└─────────────────────────┘
    ↓
┌─────────────────────────────┐   ← Multiple
├─ Backend (Spring Boot)     │  instances
├─ Microservice containers   │
├─ Docker containers         │
└─────────────────────────────┘
    ↓
"MySQL Database Cluster
├─ Master-Slave replication
├─ Automated backups
├─ Read replicas
└─ Connection pooling
```

### Containerization (Docker)

```dockerfile
# Backend (Dockerfile)
FROM openjdk:17-jdk
COPY target/clappn-api.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080

# Frontend (Dockerfile)
FROM node:18 as build
COPY . .
RUN npm install && npm run build
FROM nginx
COPY --from=build dist /usr/share/nginx/html
EXPOSE 80
```

---

## Performance Considerations

### Backend Optimization
- **Database Indexes**: Frequently queried fields indexed
- **Query Optimization**: JPA projections for read-only queries
- **Caching**: Spring Cache for frequently accessed data
- **Connection Pooling**: HikariCP for database connections
- **Async Processing**: async methods for non-blocking operations
- **Pagination**: Limit result sets, implement offset/limit

### Frontend Optimization
- **Code Splitting**: Feature-based lazy loading
- **Component Memoization**: React.memo, useMemo for expensive renders
- **Image Optimization**: Lazy load images, use WebP format
- **Bundle Size**: Tree shaking, minification via Vite
- **HTTP Caching**: Leverage browser cache, ETag headers
- **Request Batching**: Combine multiple requests where possible

### Database Optimization
- **Query Optimization**: EXPLAIN ANALYZE queries
- **Index Strategy**: Balance read/write performance
- **Normalization**: 3NF design, reduce redundancy
- **Archiving**: Move old data to archive tables
- **Partitioning**: Large tables by date or category

---

## Security Architecture

### Layers of Defense
```
1. Network Level
   ├─ HTTPS/TLS encryption
   ├─ Firewall rules
   └─ DDoS protection

2. Application Level
   ├─ JWT authentication
   ├─ Permission-based authorization
   ├─ Input validation & sanitization
   ├─ SQL parameterization (JPA)
   ├─ Output encoding (React auto-escape)
   └─ CORS configuration

3. Data Level
   ├─ Encrypted sensitive fields
   ├─ Password hashing (bcrypt)
   ├─ Audit logging
   └─ Data anonymization for PII

4. Infrastructure Level
   ├─ Secrets management
   ├─ Environment isolation
   ├─ Regular security patches
   └─ Vulnerability scanning
```

---

## Summary

The CraftLane architecture follows industry best practices:

✅ **Layered Backend**: Controller → Service → Repository → Entity  
✅ **Feature-Based Frontend**: Modules with clear boundaries  
✅ **RESTful APIs**: Standard HTTP verbs, clear endpoints  
✅ **JWT Authentication**: Stateless, scalable authentication  
✅ **RBAC Authorization**: Role + permission-based access  
✅ **Type-Safe Frontend**: TypeScript with strict mode  
✅ **Database Normalization**: Efficient schema design  
✅ **Security First**: Multiple layers of protection  
✅ **Scalable Design**: Ready for microservices conversion  
✅ **Well-Documented**: Comprehensive architecture documentation  

---

**Last Updated**: March 14, 2026  
**Architecture Version**: 2.0  
**Next Review**: Q2 2026
