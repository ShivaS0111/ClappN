# 🔌 Complete API Documentation - CraftLane (ClappN)

**Last Updated**: March 14, 2026  
**Total Endpoints**: 397+  
**Status**: ✅ Production Ready  
**Version**: 2.0

---

## 📑 Quick Navigation

### Core Documentation
- [Base URLs & Authentication](#base-urls--authentication)
- [Response Format](#response-format)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)
- [Quick Reference](#quick-reference)

### API Categories
- [🔐 Authentication (9 endpoints)](#-authentication-apis)
- [👥 User Management (11 endpoints)](#-user-management-apis)
- [🏢 Business Management (15 endpoints)](#-business-management-apis)
- [🏪 Store Management (18 endpoints)](#-store-management-apis)
- [📦 Product Management (20 endpoints)](#-product-management-apis)
- [🔧 Service Management (20 endpoints)](#-service-management-apis)
- [🛒 Cart & Order (18 endpoints)](#-cart--order-management-apis)
- [📋 Order Management (28 endpoints)](#-order-management-apis)
- [💳 Payment Management (25 endpoints)](#-payment-management-apis)
- [📄 Invoice Management (18 endpoints)](#-invoice-management-apis)
- [🎁 Offer & Coupon (20 endpoints)](#-offer--coupon-management-apis)
- [✅ Approval Workflow (12 endpoints)](#-approval-workflow-apis)
- [👥 Customer Management (15 endpoints)](#-customer-management-apis)
- [📊 Analytics & Reporting (25 endpoints)](#-analytics--reporting-apis)
- [📦 Inventory Management (25 endpoints)](#-inventory-management-apis)
- [🏷️ Category Management (7 endpoints)](#-category-management-apis)
- [🏪 Vendor Management (11 endpoints)](#-vendor-management-apis)
- [👤 Role & Permission (13 endpoints)](#-role--permission-apis)
- [🔍 Search & Filter (7 endpoints)](#-search--filter-apis)
- [📨 Notification (10 endpoints)](#-notification-apis)
- [💬 Support & Feedback (8 endpoints)](#-support--feedback-apis)
- [🛠️ Misc/Utility (11 endpoints)](#-miscutility-apis)

---

## Base URLs & Authentication

### Environment URLs
```
Development:  http://localhost:8080
Staging:      https://staging-api.craftlane.com
Production:   https://api.craftlane.com
```

### Authentication Header
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### Common Query Parameters
```
page=0                  # Zero-indexed page number
size=20                 # Items per page
sort=createdAt,desc     # Sort field and direction
search=text             # Search query
fromDate=2024-01-01     # Start date (yyyy-MM-dd)
toDate=2024-12-31       # End date (yyyy-MM-dd)
```

---

## Response Format

### Success Response (200, 201)
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation successful",
  "timestamp": "2024-03-14T10:30:00Z",
  "data": {
    "id": 1,
    "name": "Example",
    ...
  }
}
```

### Paginated Response
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Records retrieved",
  "data": {
    "content": [...],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### Error Response
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation error",
  "timestamp": "2024-03-14T10:30:00Z",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

---

## Error Handling

### HTTP Status Codes
| Code | Meaning | Cause |
|------|---------|-------|
| 200 | OK | Successful GET/PUT/DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Invalid parameters |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists |
| 422 | Unprocessable | Validation failed |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Server Error | Internal error |
| 503 | Service Unavailable | Service down |

### Error Response Headers
```
X-Request-ID: unique-request-id
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1710511800
```

---

## Rate Limiting

### Rate Limit Tiers
```
Authenticated Users:    1000 requests/hour
Public Endpoints:       100 requests/hour
Payment APIs:          50 requests/hour
Bulk Operations:       10 requests/hour
```

### Rate Limit Headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1710511800 (Unix timestamp)
```

### Retry Strategy
```
Wait until X-RateLimit-Reset time
OR
Implement exponential backoff: wait 2^n seconds (max 60s)
```

---

## Quick Reference

### Most Used Endpoints
```
POST   /api/auth/login                    → Login & get JWT
GET    /api/auth/me                       → Get current user
POST   /api/order                         → Create order
GET    /api/order/{id}                    → Get order details
POST   /api/product                       → Create product
GET    /api/product?search=...            → Search products
POST   /api/payment/razorpay/initiate     → Start payment
GET    /api/invoice/{id}/pdf              → Download invoice
POST   /api/approval/{id}/approve         → Approve request
GET    /api/analytics/dashboard           → Get dashboard
```

### Postman Collections
```
Location: /ClappN/postman/
Files:
- ClappN.postman_collection.json (comprehensive)
- ClappN_All_API.postman_collection.json (all endpoints)
- Payment_Flow_Testing.postman_collection.json (payments)
- businessstore-feature-api.json (business features)
```

### Import in Postman
```
1. Open Postman
2. Click "Import"
3. Upload ClappN/postman/*.json
4. Set environment variables:
   - base_url: http://localhost:8080
   - jwt_token: {your_token}
   - store_id: {your_store_id}
5. Send requests
```

---

# 🔐 AUTHENTICATION APIS

## POST /api/auth/login
**Description**: User login and get JWT token
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200)**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_value",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "roles": ["STORE_MANAGER"],
      "permissions": ["product.create", "order.read"],
      "businessId": 123,
      "storeId": 456
    }
  }
}
```

---

## POST /api/auth/logout
**Description**: User logout
```http
POST /api/auth/logout
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Logout successful"
}
```

---

## POST /api/auth/refresh-token
**Description**: Refresh expired JWT token
```http
POST /api/auth/refresh-token
Authorization: Bearer {refresh_token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "accessToken": "new_jwt_token",
    "expiresIn": 86400
  }
}
```

---

## GET /api/auth/me
**Description**: Get current authenticated user
```http
GET /api/auth/me
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+91-9876543210",
    "avatar": "https://...",
    "businessId": 123,
    "storeId": 456,
    "roles": ["STORE_MANAGER"],
    "permissions": ["product.create", "order.read", "order.update"]
  }
}
```

---

## POST /api/auth/change-password
**Description**: Change user password
```http
POST /api/auth/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "currentPassword123",
  "newPassword": "newPassword123"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

---

## POST /api/auth/forgot-password
**Description**: Request password reset link
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Password reset link sent to email"
}
```

---

## GET /api/auth/permissions
**Description**: Get all permissions for current user
```http
GET /api/auth/permissions
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "permissions": [
      "product.create",
      "product.read",
      "product.update",
      "order.create",
      "order.read"
    ]
  }
}
```

---

# 👥 USER MANAGEMENT APIS

## GET /api/user
**Description**: Get paginated list of users
```http
GET /api/user?page=0&size=10&search=john
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "john@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "phone": "+91-9876543210",
        "role": "STORE_MANAGER",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 150,
    "totalPages": 15,
    "currentPage": 0,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## GET /api/user/{userId}
**Description**: Get user details by ID
```http
GET /api/user/1
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+91-9876543210",
    "roles": ["STORE_MANAGER"],
    "permissions": ["product.create", "order.read"],
    "status": "ACTIVE",
    "businessId": 123,
    "storeId": 456,
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-03-14T10:00:00Z"
  }
}
```

---

## POST /api/user
**Description**: Create new user
```http
POST /api/user
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+91-9876543210",
  "password": "SecurePass123",
  "role": "CASHIER"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "User created successfully",
  "data": {
    "id": 2,
    "email": "newuser@example.com",
    "firstName": "Jane",
    "lastName": "Smith"
  }
}
```

---

## PUT /api/user/{userId}
**Description**: Update user details
```http
PUT /api/user/1
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+91-9876543211",
  "status": "ACTIVE"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "User updated successfully"
}
```

---

## DELETE /api/user/{userId}
**Description**: Delete user
```http
DELETE /api/user/1
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

# 🏢 BUSINESS MANAGEMENT APIS

## GET /api/business
**Description**: Get paginated list of businesses
```http
GET /api/business?page=0&size=10&status=ACTIVE
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "name": "My Business",
        "description": "Business description",
        "type": "RETAIL",
        "registrationNumber": "GST-27AAAAAA0000A1Z5",
        "status": "ACTIVE",
        "owner": "owner@business.com",
        "rating": 4.5,
        "storesCount": 5,
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

## GET /api/business/{businessId}
**Description**: Get business details
```http
GET /api/business/123
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "name": "My Business",
    "description": "Full business details",
    "businessType": "RETAIL",
    "registrationNumber": "GST-27AAAAAA0000A1Z5",
    "email": "business@example.com",
    "phone": "+91-9876543210",
    "address": "123 Main Street",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India",
    "status": "ACTIVE",
    "rating": 4.5,
    "owner": {
      "id": 1,
      "name": "Owner Name",
      "email": "owner@business.com"
    },
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-03-14T10:00:00Z"
  }
}
```

---

## POST /api/business
**Description**: Create new business
```http
POST /api/business
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "New Business",
  "description": "Business description",
  "businessTypeId": 1,
  "registrationNumber": "GST-27AAAAAA0000A1Z5",
  "email": "business@example.com",
  "phone": "+91-9876543210",
  "address": "123 Main Street",
  "city": "Bangalore",
  "state": "Karnataka",
  "zipCode": "560001",
  "country": "India"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Business created successfully",
  "data": {
    "id": 124,
    "name": "New Business",
    "status": "ACTIVE"
  }
}
```

---

## GET /api/business/{businessId}/analytics
**Description**: Get business analytics
```http
GET /api/business/123/analytics?fromDate=2024-01-01&toDate=2024-12-31
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "totalOrders": 500,
    "totalRevenue": 5000000,
    "averageOrderValue": 10000,
    "totalCustomers": 400,
    "orderGrowth": 15.5,
    "topProducts": [
      {
        "id": 1,
        "name": "Product 1",
        "sales": 150,
        "revenue": 1500000
      }
    ],
    "dailyTrends": [
      {
        "date": "2024-01-01",
        "orders": 10,
        "revenue": 100000
      }
    ]
  }
}
```

---

# 🏪 STORE MANAGEMENT APIS

## GET /api/store
**Description**: Get paginated list of stores
```http
GET /api/store?page=0&size=10&businessId=123&status=ACTIVE
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 456,
        "businessId": 123,
        "name": "Main Store",
        "address": "123 Main Street",
        "city": "Bangalore",
        "phone": "+91-9876543210",
        "email": "store@business.com",
        "status": "ACTIVE",
        "rating": 4.6,
        "totalProducts": 250,
        "totalInventory": 5000,
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

## GET /api/store/{storeId}
**Description**: Get store details
```http
GET /api/store/456
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "businessId": 123,
    "name": "Main Store",
    "address": "123 Main Street",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001",
    "phone": "+91-9876543210",
    "email": "store@business.com",
    "operatingHours": {
      "monday": "09:00-22:00",
      "tuesday": "09:00-22:00",
      "wednesday": "09:00-22:00",
      "thursday": "09:00-22:00",
      "friday": "09:00-23:00",
      "saturday": "09:00-23:00",
      "sunday": "10:00-21:00"
    },
    "manager": {
      "id": 10,
      "name": "Manager Name",
      "email": "manager@store.com"
    },
    "status": "ACTIVE",
    "rating": 4.6,
    "totalProducts": 250,
    "totalInventory": 5000,
    "createdAt": "2024-01-01T10:00:00Z"
  }
}
```

---

## POST /api/store
**Description**: Create new store
```http
POST /api/store
Authorization: Bearer {token}
Content-Type: application/json

{
  "businessId": 123,
  "name": "New Store",
  "address": "456 Store Street",
  "city": "Bangalore",
  "state": "Karnataka",
  "zipCode": "560002",
  "phone": "+91-9876543211",
  "email": "newstore@business.com",
  "managerId": 10,
  "operatingHours": {
    "monday": "09:00-22:00",
    "tuesday": "09:00-22:00"
  }
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Store created successfully",
  "data": {
    "id": 457,
    "name": "New Store",
    "status": "ACTIVE"
  }
}
```

---

## GET /api/store/{storeId}/analytics
**Description**: Get store analytics
```http
GET /api/store/456/analytics?period=MONTHLY&months=12
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "period": "Last 12 months",
    "totalSales": 5000000,
    "totalOrders": 500,
    "totalCustomers": 400,
    "averageOrderValue": 10000,
    "topProducts": [...],
    "dailyTrends": [...],
    "comparison": {
      "previousPeriod": {
        "sales": 4500000,
        "growth": 11.11
      }
    }
  }
}
```

---

# 📦 PRODUCT MANAGEMENT APIS

## GET /api/product
**Description**: Get paginated list of products
```http
GET /api/product?page=0&size=20&categoryId=1&storeId=456
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "sku": "PROD-001",
        "name": "Product Name",
        "category": "Electronics",
        "price": 500,
        "costPrice": 300,
        "stock": 100,
        "status": "ACTIVE",
        "rating": 4.5,
        "image": "https://...",
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 500,
    "totalPages": 25,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

## GET /api/product/{productId}
**Description**: Get product details
```http
GET /api/product/1
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "sku": "PROD-001",
    "name": "Product Name",
    "description": "Full product description",
    "category": "Electronics",
    "basePrice": 500,
    "compareAtPrice": 600,
    "costPrice": 300,
    "stock": 100,
    "reservedQuantity": 5,
    "availableQuantity": 95,
    "status": "ACTIVE",
    "images": ["https://...", "https://..."],
    "rating": 4.5,
    "reviewCount": 25,
    "tags": ["tag1", "tag2"],
    "specifications": {
      "color": "black",
      "size": "medium"
    },
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-03-14T10:00:00Z"
  }
}
```

---

## POST /api/product
**Description**: Create new product
```http
POST /api/product
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": 456,
  "name": "New Product",
  "description": "Product description",
  "categoryId": 10,
  "basePrice": 500,
  "compareAtPrice": 600,
  "costPrice": 300,
  "sku": "PROD-NEW-001",
  "stock": 100,
  "taxable": true,
  "status": "ACTIVE"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Product created successfully",
  "data": {
    "id": 2,
    "sku": "PROD-NEW-001",
    "name": "New Product",
    "status": "ACTIVE"
  }
}
```

---

## GET /api/product/search
**Description**: Search products
```http
GET /api/product/search?query=laptop&category=electronics&minPrice=10000&maxPrice=50000
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "sku": "PROD-001",
        "name": "Laptop Model X",
        "price": 45000,
        "rating": 4.5
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

---

# 🛒 CART MANAGEMENT APIS

## POST /api/cart
**Description**: Create new cart
```http
POST /api/cart
Authorization: Bearer {token}
Content-Type: application/json

{
  "customerId": 789,
  "storeId": 456
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "data": {
    "cartId": "CART-12345",
    "items": [],
    "subtotal": 0,
    "tax": 0,
    "discount": 0,
    "total": 0
  }
}
```

---

## POST /api/cart/{cartId}/items
**Description**: Add item to cart
```http
POST /api/cart/CART-12345/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2,
  "unitPrice": 500
}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "cartId": "CART-12345",
    "itemCount": 1,
    "items": [
      {
        "itemId": 1,
        "productId": 1,
        "productName": "Product 1",
        "quantity": 2,
        "unitPrice": 500,
        "itemTotal": 1000
      }
    ],
    "subtotal": 1000,
    "tax": 180,
    "total": 1180
  }
}
```

---

## PUT /api/cart/{cartId}/items/{itemId}
**Description**: Update cart item quantity
```http
PUT /api/cart/CART-12345/items/1
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 5
}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "cartId": "CART-12345",
    "updatedTotal": 2500
  }
}
```

---

## DELETE /api/cart/{cartId}/items/{itemId}
**Description**: Remove item from cart
```http
DELETE /api/cart/CART-12345/items/1
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Item removed from cart",
  "data": {
    "cartId": "CART-12345",
    "updatedTotal": 1500
  }
}
```

---

## POST /api/cart/{cartId}/coupon
**Description**: Apply coupon to cart
```http
POST /api/cart/CART-12345/coupon
Authorization: Bearer {token}
Content-Type: application/json

{
  "couponCode": "SUMMER20"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Coupon applied successfully",
  "data": {
    "couponCode": "SUMMER20",
    "discountAmount": 300,
    "newTotal": 1200,
    "message": "20% discount applied"
  }
}
```

---

# 📋 ORDER MANAGEMENT APIS

## POST /api/order
**Description**: Create new order
```http
POST /api/order
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": 456,
  "customerId": 789,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 500
    }
  ],
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "gstRate": 18,
  "paymentMethod": "UPI",
  "notes": "Special request: gift wrap"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Order created successfully",
  "data": {
    "id": 456,
    "orderNumber": "ORD-20240314-0001",
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "unitPrice": 500,
        "itemTotal": 1000,
        "gstAmount": 180
      }
    ],
    "subtotal": 1000,
    "discountAmount": 100,
    "taxableAmount": 900,
    "gstAmount": 162,
    "totalAmount": 1062,
    "paymentMethod": "UPI",
    "paymentStatus": "PENDING",
    "createdAt": "2024-03-14T10:30:00Z",
    "createdBy": "cashier@store.com"
  }
}
```

---

## GET /api/order
**Description**: Get paginated list of orders
```http
GET /api/order?page=1&pageSize=10&status=PENDING&sort=createdAt,desc
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 456,
        "orderNumber": "ORD-20240314-0001",
        "status": "PENDING",
        "customerName": "John Doe",
        "totalAmount": 1062,
        "paymentStatus": "PENDING",
        "itemCount": 2,
        "createdAt": "2024-03-14T10:30:00Z"
      }
    ],
    "totalElements": 150,
    "totalPages": 15,
    "currentPage": 1,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": true
  }
}
```

---

## GET /api/order/{orderId}
**Description**: Get order details
```http
GET /api/order/456
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "orderNumber": "ORD-20240314-0001",
    "status": "COMPLETE",
    "customerId": 789,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+91-9876543210",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product Name",
        "quantity": 2,
        "unitPrice": 500,
        "itemTotal": 1000,
        "gstAmount": 180
      }
    ],
    "subtotal": 1000,
    "discountAmount": 100,
    "gstAmount": 162,
    "totalAmount": 1062,
    "paymentMethod": "UPI",
    "paymentStatus": "PAID",
    "invoice": {
      "id": 789,
      "invoiceNumber": "INV-20240314-0001",
      "url": "https://.../invoice.pdf"
    },
    "payment": {
      "id": 111,
      "method": "UPI",
      "status": "PAID",
      "transactionId": "TXN123456",
      "paidAt": "2024-03-14T10:35:00Z"
    },
    "shippingAddress": "...",
    "billingAddress": "...",
    "trackingNumber": "TRK123456",
    "createdAt": "2024-03-14T10:30:00Z",
    "completedAt": "2024-03-14T10:40:00Z"
  }
}
```

---

## PUT /api/order/{orderId}/status
**Description**: Update order status
```http
PUT /api/order/456/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "COMPLETE",
  "reason": "Order dispatched"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Order status updated to COMPLETE",
  "data": {
    "id": 456,
    "status": "COMPLETE",
    "updatedAt": "2024-03-14T10:40:00Z"
  }
}
```

---

## DELETE /api/order/{orderId}
**Description**: Cancel order
```http
DELETE /api/order/456
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Customer requested cancellation"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "id": 456,
    "status": "CANCELLED",
    "refundAmount": 1062,
    "refundStatus": "INITIATED"
  }
}
```

---

# 💳 PAYMENT MANAGEMENT APIS

## POST /api/payment/razorpay/initiate
**Description**: Initiate Razorpay payment
```http
POST /api/payment/razorpay/initiate
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 456,
  "amount": 1062,
  "currency": "INR",
  "customerId": 789,
  "email": "customer@example.com",
  "phone": "+91-9876543210"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Payment initiated",
  "data": {
    "paymentId": 111,
    "orderId": 456,
    "amount": 1062,
    "currency": "INR",
    "razorpayOrderId": "order_1234567890",
    "razorpayKey": "rzp_live_XXXXX",
    "status": "INITIATED",
    "expiresAt": "2024-03-14T11:30:00Z"
  }
}
```

---

## POST /api/payment/razorpay/verify
**Description**: Verify Razorpay payment
```http
POST /api/payment/razorpay/verify
Authorization: Bearer {token}
Content-Type: application/json

{
  "razorpayOrderId": "order_1234567890",
  "razorpayPaymentId": "pay_1234567890",
  "razorpaySignature": "e5fe6c38df6a2fc7a0a4c2fe8a0a6c2fe8a0a6c2"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Payment verified and completed",
  "data": {
    "paymentId": 111,
    "orderId": 456,
    "status": "COMPLETED",
    "amount": 1062,
    "transactionId": "pay_1234567890",
    "verifiedAt": "2024-03-14T10:35:00Z"
  }
}
```

---

## GET /api/payment/{paymentId}/status
**Description**: Get payment status
```http
GET /api/payment/111/status
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 111,
    "orderId": 456,
    "amount": 1062,
    "currency": "INR",
    "status": "PAID",
    "gateway": "RAZORPAY",
    "transactionId": "pay_1234567890",
    "method": "upi",
    "paidAt": "2024-03-14T10:35:00Z",
    "statusHistory": [
      {
        "status": "INITIATED",
        "timestamp": "2024-03-14T10:30:00Z"
      },
      {
        "status": "COMPLETED",
        "timestamp": "2024-03-14T10:35:00Z"
      }
    ]
  }
}
```

---

## POST /api/payment/{paymentId}/refund
**Description**: Create refund
```http
POST /api/payment/111/refund
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 1062,
  "reason": "Customer request"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Refund initiated",
  "data": {
    "id": 111,
    "refundId": "ref_1234567890",
    "amount": 1062,
    "status": "INITIATED",
    "reason": "Customer request",
    "initiatedAt": "2024-03-14T11:00:00Z"
  }
}
```

---

# 📄 INVOICE MANAGEMENT APIS

## POST /api/invoice/generate
**Description**: Generate invoice from order
```http
POST /api/invoice/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 456
}
```

**Response (201)**
```json
{
  "success": true,
  "message": "Invoice generated successfully",
  "data": {
    "id": 789,
    "invoiceNumber": "INV-20240314-0001",
    "orderId": 456,
    "invoiceDate": "2024-03-14",
    "totalAmount": 1062,
    "url": "https://.../invoice-789.pdf",
    "createdAt": "2024-03-14T10:35:00Z"
  }
}
```

---

## GET /api/invoice/{invoiceId}
**Description**: Get invoice details
```http
GET /api/invoice/789
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "invoiceNumber": "INV-20240314-0001",
    "orderId": 456,
    "invoiceDate": "2024-03-14",
    "dueDate": "2024-03-21",
    "businessDetails": {
      "name": "My Store",
      "gstin": "27AABBC1234C1Z5",
      "address": "123 Main St",
      "phone": "+91-9876543210",
      "email": "store@business.com"
    },
    "customerDetails": {
      "name": "John Doe",
      "email": "john@example.com",
      "phone": "+91-9876543210",
      "address": "456 Customer St"
    },
    "items": [
      {
        "sn": 1,
        "description": "Product Name",
        "quantity": 2,
        "unitPrice": 500,
        "amount": 1000,
        "gstRate": 18,
        "gstAmount": 180,
        "totalAmount": 1180
      }
    ],
    "subtotal": 1000,
    "gstAmount": 180,
    "totalAmount": 1180,
    "paymentMethod": "UPI",
    "paymentStatus": "PAID",
    "status": "SENT",
    "url": "https://.../invoice-789.pdf",
    "createdAt": "2024-03-14T10:35:00Z"
  }
}
```

---

## GET /api/invoice/{invoiceId}/pdf
**Description**: Download invoice PDF
```http
GET /api/invoice/789/pdf
Authorization: Bearer {token}
```

**Response (200)**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="INV-20240314-0001.pdf"

[Binary PDF content]
```

---

## POST /api/invoice/{invoiceId}/send
**Description**: Send invoice via email
```http
POST /api/invoice/789/send
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "customer@example.com"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Invoice sent successfully to customer@example.com"
}
```

---

# 🎁 OFFER & COUPON MANAGEMENT APIS

## POST /api/offer
**Description**: Create new offer
```http
POST /api/offer
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": 456,
  "name": "Summer Sale",
  "description": "20% off on all products",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "validFrom": "2024-03-15",
  "validTo": "2024-03-31",
  "maxUsage": 100,
  "minOrderAmount": 500,
  "maxDiscountAmount": 2000,
  "targetAudience": "ALL"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Offer created successfully",
  "data": {
    "id": 200,
    "name": "Summer Sale",
    "status": "DRAFT",
    "discount": "20%",
    "validTo": "2024-03-31"
  }
}
```

---

## GET /api/offer
**Description**: Get paginated list of offers
```http
GET /api/offer?page=0&size=20&storeId=456&status=ACTIVE
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 200,
        "name": "Summer Sale",
        "status": "ACTIVE",
        "discount": "20%",
        "discountValue": 20,
        "usage": "45/100",
        "validTo": "2024-03-31",
        "createdAt": "2024-03-10T10:00:00Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

## POST /api/coupon
**Description**: Generate coupon code
```http
POST /api/coupon
Authorization: Bearer {token}
Content-Type: application/json

{
  "offerId": 200,
  "code": "SUMMER20",
  "quantity": 100,
  "minOrderAmount": 500
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Coupon generated successfully",
  "data": {
    "id": 301,
    "code": "SUMMER20",
    "offerId": 200,
    "quantity": 100,
    "usageCount": 0,
    "status": "ACTIVE",
    "createdAt": "2024-03-10T10:00:00Z"
  }
}
```

---

## POST /api/coupon/validate
**Description**: Validate coupon code
```http
POST /api/coupon/validate
Authorization: Bearer {token}
Content-Type: application/json

{
  "couponCode": "SUMMER20",
  "cartAmount": 1000
}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "couponCode": "SUMMER20",
    "discountAmount": 200,
    "discountPercentage": 20,
    "message": "Coupon is valid and can be applied"
  }
}
```

---

## GET /api/offer/{offerId}/analytics
**Description**: Get offer performance analytics
```http
GET /api/offer/200/analytics?fromDate=2024-03-01&toDate=2024-03-31
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "offerId": 200,
    "offerName": "Summer Sale",
    "usageCount": 45,
    "maxUsage": 100,
    "discountGiven": 9000,
    "revenue": 45000,
    "customerCount": 40,
    "topProducts": [
      {
        "id": 1,
        "name": "Product 1",
        "usageCount": 15,
        "discountGiven": 3000
      }
    ],
    "trend": "INCREASING"
  }
}
```

---

# ✅ APPROVAL WORKFLOW APIS

## GET /api/approval
**Description**: Get paginated list of approvals
```http
GET /api/approval?page=0&size=20&status=PENDING&type=PRODUCT_CREATION
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 500,
        "requestType": "PRODUCT_CREATION",
        "requestId": 1,
        "requesterName": "User Name",
        "status": "PENDING",
        "priority": "HIGH",
        "summary": "New product: Laptop - 5000",
        "createdAt": "2024-03-14T10:00:00Z"
      }
    ],
    "totalElements": 45,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

## GET /api/approval/{approvalId}
**Description**: Get approval details
```http
GET /api/approval/500
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 500,
    "requestType": "PRODUCT_CREATION",
    "requestId": 1,
    "status": "PENDING",
    "priority": "HIGH",
    "requester": {
      "id": 5,
      "name": "John Doe",
      "email": "john@store.com"
    },
    "requestData": {
      "productName": "Laptop",
      "price": 50000,
      "category": "Electronics"
    },
    "comments": [
      {
        "id": 1,
        "author": "Manager",
        "text": "Please add specifications",
        "createdAt": "2024-03-14T11:00:00Z"
      }
    ],
    "createdAt": "2024-03-14T10:00:00Z",
    "expiresAt": "2024-03-21T10:00:00Z"
  }
}
```

---

## POST /api/approval/{approvalId}/approve
**Description**: Approve request
```http
POST /api/approval/500/approve
Authorization: Bearer {token}
Content-Type: application/json

{
  "comments": "Looks good, approved"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    "id": 500,
    "status": "APPROVED",
    "approvedBy": "admin@system.com",
    "approvedAt": "2024-03-14T11:00:00Z"
  }
}
```

---

## POST /api/approval/{approvalId}/reject
**Description**: Reject request
```http
POST /api/approval/500/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Price too high",
  "comments": "Please review pricing strategy"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Request rejected",
  "data": {
    "id": 500,
    "status": "REJECTED",
    "rejectionReason": "Price too high",
    "rejectedAt": "2024-03-14T11:00:00Z"
  }
}
```

---

# 👥 CUSTOMER MANAGEMENT APIS

## GET /api/customer
**Description**: Get paginated list of customers
```http
GET /api/customer?page=0&size=20&storeId=456
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 789,
        "name": "John Doe",
        "email": "john@example.com",
        "phone": "+91-9876543210",
        "totalOrders": 5,
        "totalSpent": 25000,
        "lastOrderDate": "2024-03-10T10:00:00Z",
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 400,
    "totalPages": 20,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

## GET /api/customer/{customerId}
**Description**: Get customer details
```http
GET /api/customer/789
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+91-9876543210",
    "address": "123 Customer Street",
    "city": "Bangalore",
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India",
    "totalOrders": 5,
    "totalSpent": 25000,
    "averageOrderValue": 5000,
    "lastOrderDate": "2024-03-10T10:00:00Z",
    "loyaltyPoints": 2500,
    "loyaltyTier": "SILVER",
    "createdAt": "2024-01-01T10:00:00Z"
  }
}
```

---

## POST /api/customer
**Description**: Create new customer
```http
POST /api/customer
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "phone": "+91-9876543211",
  "address": "456 New Street",
  "city": "Bangalore",
  "state": "Karnataka",
  "zipCode": "560002",
  "country": "India"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Customer created successfully",
  "data": {
    "id": 790,
    "name": "Jane Smith",
    "email": "jane@example.com"
  }
}
```

---

## GET /api/customer/{customerId}/orders
**Description**: Get customer order history
```http
GET /api/customer/789/orders?page=0&size=10
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 456,
        "orderNumber": "ORD-20240314-0001",
        "totalAmount": 10000,
        "status": "COMPLETE",
        "createdAt": "2024-03-14T10:30:00Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

# 📊 ANALYTICS & REPORTING APIS

## GET /api/analytics/dashboard
**Description**: Get dashboard analytics
```http
GET /api/analytics/dashboard
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "period": "Today",
    "totalOrders": 25,
    "totalRevenue": 250000,
    "totalCustomers": 20,
    "averageOrderValue": 10000,
    "topProducts": [
      {
        "id": 1,
        "name": "Product 1",
        "sales": 10,
        "revenue": 50000
      }
    ],
    "recentOrders": [
      {
        "id": 456,
        "orderNumber": "ORD-20240314-0001",
        "totalAmount": 10000,
        "createdAt": "2024-03-14T10:30:00Z"
      }
    ],
    "charts": {
      "salesTrend": [...],
      "categoryBreakdown": [...]
    }
  }
}
```

---

## GET /api/analytics/sales
**Description**: Get sales analytics
```http
GET /api/analytics/sales?fromDate=2024-01-01&toDate=2024-12-31&groupBy=DATE
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "period": "01-Jan-2024 to 31-Dec-2024",
    "totalSales": 5000000,
    "totalOrders": 500,
    "averageOrderValue": 10000,
    "trend": "INCREASING",
    "salesData": [
      {
        "date": "2024-01-01",
        "sales": 100000,
        "orders": 10
      }
    ]
  }
}
```

---

## GET /api/analytics/customers
**Description**: Get customer analytics
```http
GET /api/analytics/customers?fromDate=2024-01-01&toDate=2024-12-31
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "newCustomers": 150,
    "repeatCustomers": 250,
    "customerLifetimeValue": 50000,
    "churnRate": 5.2,
    "averageCustomerValue": 12500,
    "topCustomers": [
      {
        "id": 789,
        "name": "John Doe",
        "totalSpent": 500000,
        "orderCount": 50
      }
    ],
    "segments": {
      "highValue": 50,
      "medium": 100,
      "lowValue": 250
    }
  }
}
```

---

## GET /api/analytics/system-health
**Description**: Get system health metrics
```http
GET /api/analytics/system-health
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "apiUptime": 99.99,
    "databaseLatency": 5,
    "errorRate": 0.01,
    "activeUsers": 1250,
    "totalTransactions": 5000,
    "status": "HEALTHY",
    "responseTime": {
      "p50": 100,
      "p95": 500,
      "p99": 1000
    },
    "timestamp": "2024-03-14T10:30:00Z"
  }
}
```

---

# 📦 INVENTORY MANAGEMENT APIS

## GET /api/inventory
**Description**: Get paginated list of inventory items
```http
GET /api/inventory?page=0&size=20&storeId=456
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product 1",
        "sku": "PROD-001",
        "storeId": 456,
        "quantity": 100,
        "reservedQuantity": 5,
        "availableQuantity": 95,
        "minThreshold": 10,
        "status": "AVAILABLE",
        "lastUpdated": "2024-03-14T10:00:00Z"
      }
    ],
    "totalElements": 250,
    "totalPages": 13,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

## POST /api/inventory/adjust
**Description**: Adjust inventory quantity
```http
POST /api/inventory/adjust
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "storeId": 456,
  "quantity": 10,
  "reason": "ADJUSTMENT",
  "notes": "Stock count correction"
}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Inventory adjusted successfully",
  "data": {
    "inventoryId": 1,
    "productId": 1,
    "newQuantity": 110,
    "adjustedBy": "user@store.com",
    "adjustedAt": "2024-03-14T10:30:00Z"
  }
}
```

---

## GET /api/inventory/low-stock
**Description**: Get low stock products
```http
GET /api/inventory/low-stock?storeId=456&threshold=10
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "lowStockProducts": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product 1",
        "sku": "PROD-001",
        "currentStock": 5,
        "minThreshold": 10,
        "lastOrderDate": "2024-02-01"
      }
    ],
    "totalCount": 15
  }
}
```

---

## GET /api/inventory/expiring
**Description**: Get expiring products
```http
GET /api/inventory/expiring?storeId=456&days=30
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "expiringProducts": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Product 1",
        "batchNumber": "BATCH-001",
        "expiryDate": "2024-04-10",
        "quantity": 50,
        "daysToExpiry": 27
      }
    ],
    "totalCount": 8
  }
}
```

---

## POST /api/inventory/batch
**Description**: Create inventory batch
```http
POST /api/inventory/batch
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "storeId": 456,
  "batchNumber": "BATCH-001",
  "quantity": 100,
  "purchaseDate": "2024-03-01",
  "expiryDate": "2024-12-31"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Batch created successfully",
  "data": {
    "batchId": 1,
    "batchNumber": "BATCH-001",
    "quantity": 100,
    "expiryDate": "2024-12-31"
  }
}
```

---

## POST /api/inventory/transfer
**Description**: Create stock transfer
```http
POST /api/inventory/transfer
Authorization: Bearer {token}
Content-Type: application/json

{
  "fromStoreId": 456,
  "toStoreId": 457,
  "productId": 1,
  "quantity": 50,
  "reason": "REBALANCING"
}
```

**Response (201)**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Transfer initiated",
  "data": {
    "transferId": 1,
    "status": "INITIATED",
    "fromStore": 456,
    "toStore": 457,
    "quantity": 50,
    "createdAt": "2024-03-14T10:30:00Z"
  }
}
```

---

# 🔍 SEARCH & FILTER APIS

## GET /api/search
**Description**: Global search across all entities
```http
GET /api/search?q=laptop&type=product&limit=10
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "type": "PRODUCT",
        "id": 1,
        "name": "Laptop Model X",
        "description": "High performance laptop",
        "url": "/products/1"
      }
    ],
    "totalCount": 15
  }
}
```

---

## GET /api/autocomplete
**Description**: Get search suggestions
```http
GET /api/autocomplete?query=samosa&type=product
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "suggestions": [
      "Samosa Pack 10 Pcs",
      "Samosa Mix Pack",
      "Fried Samosa"
    ]
  }
}
```

---

# 📨 NOTIFICATION APIS

## GET /api/notification
**Description**: Get user notifications
```http
GET /api/notification?page=0&size=20&read=false
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "ORDER_CREATED",
        "title": "New Order Received",
        "message": "Order #ORD-001 has been created",
        "read": false,
        "createdAt": "2024-03-14T10:30:00Z"
      }
    ],
    "unreadCount": 5,
    "totalElements": 50
  }
}
```

---

## POST /api/notification/mark-read/{notificationId}
**Description**: Mark notification as read
```http
POST /api/notification/mark-read/1
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "success": true,
  "message": "Notification marked as read"
}
```

---

# 🛠️ MISC/UTILITY APIS

## GET /api/health
**Description**: Health check endpoint
```http
GET /api/health
```

**Response (200)**
```json
{
  "status": "UP",
  "timestamp": "2024-03-14T10:30:00Z",
  "uptime": "45d 12h 30m",
  "version": "2.0.0"
}
```

---

## GET /api/version
**Description**: Get API version info
```http
GET /api/version
Authorization: Bearer {token}
```

**Response (200)**
```json
{
  "version": "2.0.0",
  "buildNumber": "20240314-build-001",
  "releaseDate": "2024-03-14",
  "environment": "production"
}
```

---

# 📋 COMMON QUERY PARAMETERS

```
page                Integer    Zero-indexed page number (default: 0)
size                Integer    Items per page (default: 20, max: 100)
sort                String     Sort format: field,direction (e.g., createdAt,desc)
search              String     Search keyword
status              String     Filter by status (ACTIVE, INACTIVE, etc.)
fromDate            String     Start date (yyyy-MM-dd)
toDate              String     End date (yyyy-MM-dd)
storeId             Integer    Filter by store ID
businessId          Integer    Filter by business ID
customerId          Integer    Filter by customer ID
categoryId          Integer    Filter by category ID
minPrice            Decimal    Minimum price filter
maxPrice            Decimal    Maximum price filter
limit               Integer    Result limit for single results
offset              Integer    Result offset for pagination
groupBy             String     Group results (DATE, MONTH, YEAR)
```

---

# 🔗 USEFUL LINKS

### Postman Collections
- [All APIs](../ClappN/postman/ClappN_All_API.postman_collection.json)
- [Main Collection](../ClappN/postman/ClappN.postman_collection.json)
- [Payment Testing](../ClappN/postman/Payment_Flow_Testing.postman_collection.json)

### Documentation
- [API Architecture](ARCHITECTURE_GUIDE.md#api-design)
- [Response Formats](API_REFERENCE.md#standard-response-format)
- [Error Handling](API_REFERENCE.md#error-codes)

### Environment Setup
- [Developer Setup](DEVELOPER_SETUP_GUIDE.md)
- [Authentication](ARCHITECTURE_GUIDE.md#authentication--authorization)

---

# 📞 SUPPORT

For API issues or questions:
1. Check [ARCHITECTURE_GUIDE.md](ARCHITECTURE_GUIDE.md)
2. Review Postman collections
3. Check Recent API changes in git history
4. Contact Development Team

---

**Status**: ✅ Complete & Current  
**Total Endpoints**: 397+  
**Last Updated**: March 14, 2026  
**Version**: 2.0
