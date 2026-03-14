# 🔌 CraftLane API Reference

**Last Updated**: March 14, 2026  
**Base URL**: `http://localhost:8080/api`  
**Format**: JSON Request/Response  
**Authentication**: Bearer JWT Token

---

## Table of Contents
1. [Authentication APIs](#authentication-apis)
2. [Order Management APIs](#order-management-apis)
3. [Invoice APIs](#invoice-apis)
4. [Payment APIs](#payment-apis)
5. [Product APIs](#product-apis)
6. [Service APIs](#service-apis)
7. [Offer & Coupon APIs](#offer--coupon-apis)
8. [Approval Workflow APIs](#approval-workflow-apis)
9. [Business & Store APIs](#business--store-apis)
10. [Analytics & Reporting APIs](#analytics--reporting-apis)
11. [Error Codes](#error-codes)
12. [Rate Limiting](#rate-limiting)

---

## Authentication APIs

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "success": true,
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9....",
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

### Refresh Token
```http
POST /auth/refresh
Authorization: Bearer <refresh_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "new_jwt_token",
    "expiresIn": 86400
  }
}
```

### Logout
```http
POST /auth/logout
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "message": "Logout successful"
}
```

### Get Current User
```http
GET /auth/me
Authorization: Bearer <jwt_token>

Response: 200 OK
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

## Order Management APIs

### Create Order
```http
POST /order
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "storeId": 456,
  "customerId": 789,  /* Optional */
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 500
    },
    {
      "serviceId": 5,
      "quantity": 1,
      "unitPrice": 1000
    }
  ],
  "discountType": "PERCENTAGE",  /* PERCENTAGE or FIXED_AMOUNT */
  "discountValue": 10,
  "gstRate": 18,
  "paymentMethod": "UPI",  /* CASH, CARD, UPI, WALLET, OTHER */
  "paymentGateway": "RAZORPAY",  /* Optional for pre-auth */
  "notes": "Special request: gift wrap"
}

Response: 201 CREATED
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
    "subtotal": 1500,
    "discountAmount": 150,
    "taxableAmount": 1350,
    "gstAmount": 243,
    "totalAmount": 1593,
    "paymentMethod": "UPI",
    "paymentStatus": "PENDING",
    "createdAt": "2024-03-14T10:30:00Z",
    "createdBy": "cashier@store.com"
  }
}

Error: 400 BAD REQUEST
{
  "success": false,
  "statusCode": 400,
  "message": "Validation error",
  "errors": [
    {
      "field": "items",
      "message": "At least one item is required"
    }
  ]
}
```

### Get Orders List
```http
GET /order?page=1&pageSize=10&status=PENDING&sort=createdAt,desc
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 456,
        "orderNumber": "ORD-20240314-0001",
        "status": "PENDING",
        "customerName": "John Doe",
        "totalAmount": 1593,
        "paymentStatus": "PENDING",
        "createdAt": "2024-03-14T10:30:00Z"
      }
    ],
    "totalElements": 150,
    "totalPages": 15,
    "currentPage": 1,
    "pageSize": 10
  }
}
```

### Get Order Details
```http
GET /order/456
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 456,
    "orderNumber": "ORD-20240314-0001",
    "status": "COMPLETE",
    "items": [ /* full details */ ],
    "subtotal": 1500,
    "discountAmount": 150,
    "gstAmount": 243,
    "totalAmount": 1593,
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
    "createdAt": "2024-03-14T10:30:00Z",
    "completedAt": "2024-03-14T10:40:00Z"
  }
}
```

### Update Order Status
```http
PUT /order/456/status
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "status": "COMPLETE",  /* PENDING, PROCESSING, COMPLETE, CANCELLED */
  "reason": "Order dispatched"  /* Optional for cancellation */
}

Response: 200 OK
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

### Cancel Order
```http
DELETE /order/456
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "reason": "Customer requested cancellation"
}

Response: 200 OK
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "id": 456,
    "status": "CANCELLED",
    "refundAmount": 1593,
    "refundStatus": "INITIATED"
  }
}
```

---

## Invoice APIs

### Generate Invoice
```http
POST /invoice/generate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "orderId": 456
}

Response: 201 CREATED
{
  "success": true,
  "message": "Invoice generated successfully",
  "data": {
    "id": 789,
    "invoiceNumber": "INV-20240314-0001",
    "orderId": 456,
    "invoiceDate": "2024-03-14",
    "items": [ /* line items from order */ ],
    "subtotal": 1500,
    "gstAmount": 243,
    "totalAmount": 1593,
    "url": "https://.../invoice-789.pdf",
    "createdAt": "2024-03-14T10:35:00Z"
  }
}
```

### Get Invoice
```http
GET /invoice/789
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 789,
    "invoiceNumber": "INV-20240314-0001",
    "orderId": 456,
    "businessDetails": {
      "name": "My Store",
      "gstin": "27AABBC1234C1Z5",
      "address": "123 Main St"
    },
    "items": [ /* detailed line items */ ],
    "subtotal": 1500,
    "gstAmount": 243,
    "totalAmount": 1593,
    "paymentMethod": "UPI",
    "status": "GENERATED",
    "url": "https://.../invoice-789.pdf"
  }
}
```

### Download Invoice PDF
```http
GET /invoice/789/pdf
Authorization: Bearer <jwt_token>

Response: 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="INV-20240314-0001.pdf"

[Binary PDF content]
```

### List Invoices
```http
GET /invoice?startDate=2024-03-01&endDate=2024-03-31&page=1&pageSize=20
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [ /* invoice list */ ],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 1
  }
}
```

---

## Payment APIs

### Initiate Payment
```http
POST /payment/initiate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "orderId": 456,
  "amount": 1593,
  "currency": "INR",
  "gateway": "RAZORPAY",  /* RAZORPAY or STRIPE */
  "returnUrl": "https://app.example.com/payment-success",
  "cancelUrl": "https://app.example.com/payment-cancel"
}

Response: 200 OK
{
  "success": true,
  "message": "Payment initiated",
  "data": {
    "paymentId": 111,
    "orderId": 456,
    "amount": 1593,
    "currency": "INR",
    "gateway": "RAZORPAY",
    "status": "INITIATED",
    "paymentPageUrl": "https://razorpay.com/payment/...",
    "expiresAt": "2024-03-14T11:30:00Z"
  }
}
```

### Payment Webhook (Razorpay)
```http
POST /payment/webhook/razorpay
Content-Type: application/json

{
  "event": "payment.authorized",
  "payload": {
    "payment": {
      "entity": "payment",
      "id": "pay_123456",
      "entity_id": "order_id",
      "amount": 159300,
      "currency": "INR",
      "status": "authorized",
      "method": "upi"
    }
  }
}

Response: 200 OK
{
  "success": true,
  "message": "Webhook processed"
}
```

### Get Payment Status
```http
GET /payment/111
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 111,
    "orderId": 456,
    "amount": 1593,
    "currency": "INR",
    "gateway": "RAZORPAY",
    "status": "PAID",
    "transactionId": "pay_123456",
    "method": "upi",
    "paidAt": "2024-03-14T10:35:00Z",
    "receiptUrl": "https://..."
  }
}
```

### Refund Payment
```http
POST /payment/111/refund
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 1593,  /* Optional, full refund if omitted */
  "reason": "Customer request"
}

Response: 200 OK
{
  "success": true,
  "message": "Refund initiated",
  "data": {
    "id": 111,
    "refundId": "ref_123456",
    "amount": 1593,
    "status": "INITIATED",
    "reason": "Customer request",
    "initiatedAt": "2024-03-14T11:00:00Z"
  }
}
```

---

## Product APIs

### Create Product
```http
POST /product
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "storeId": 456,
  "name": "Product Name",
  "sku": "PROD-001",
  "description": "Product description",
  "categoryId": 10,
  "price": 500,
  "costPrice": 300,
  "quantity": 100,
  "status": "ACTIVE",
  "images": ["https://...", "https://..."],
  "tags": ["tag1", "tag2"]
}

Response: 201 CREATED
{
  "success": true,
  "data": {
    "id": 1,
    "sku": "PROD-001",
    "name": "Product Name",
    "price": 500,
    "quantity": 100,
    "status": "ACTIVE",
    "createdAt": "2024-03-14T10:30:00Z"
  }
}
```

### Get Products List
```http
GET /product?storeId=456&categoryId=10&search=product&page=1&pageSize=20
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "sku": "PROD-001",
        "name": "Product Name",
        "price": 500,
        "quantity": 100,
        "category": "Electronics",
        "image": "https://..."
      }
    ],
    "totalElements": 500,
    "totalPages": 25,
    "currentPage": 1
  }
}
```

### Get Product Details
```http
GET /product/1
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "sku": "PROD-001",
    "name": "Product Name",
    "description": "Full description",
    "price": 500,
    "costPrice": 300,
    "quantity": 100,
    "category": "Electronics",
    "rating": 4.5,
    "reviewCount": 25,
    "images": ["url1", "url2"],
    "tags": ["tag1", "tag2"],
    "status": "ACTIVE",
    "createdAt": "2024-03-14T10:30:00Z",
    "updatedAt": "2024-03-14T11:00:00Z"
  }
}
```

### Update Product
```http
PUT /product/1
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Updated Product Name",
  "price": 550,
  "quantity": 95,
  "status": "ACTIVE"
}

Response: 200 OK
{
  "success": true,
  "message": "Product updated successfully",
  "data": { /* updated product object */ }
}
```

### Delete Product
```http
DELETE /product/1
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "message": "Product deleted successfully"
}
```

### Search Products
```http
GET /product/search?query=laptop&categoryId=10&maxPrice=50000
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [ /* filtered products */ ],
    "totalElements": 50
  }
}
```

---

## Service APIs

### Create Service
```http
POST /service
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "storeId": 456,
  "name": "Hair Cutting",
  "description": "Professional hair cutting service",
  "price": 300,
  "durationMinutes": 30,
  "availability": "AVAILABLE",
  "images": ["https://..."],
  "category": "Beauty"
}

Response: 201 CREATED
{
  "success": true,
  "data": {
    "id": 5,
    "name": "Hair Cutting",
    "price": 300,
    "duration": "30 min",
    "status": "ACTIVE"
  }
}
```

### Get Services
```http
GET /service?storeId=456&page=1&pageSize=20
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "name": "Hair Cutting",
        "price": 300,
        "duration": "30 min",
        "rating": 4.8,
        "availability": "AVAILABLE"
      }
    ],
    "totalElements": 25
  }
}
```

### Book Service
```http
POST /service/5/book
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "customerId": 789,
  "serviceDate": "2024-03-15",
  "timeSlot": "10:30",
  "quantity": 1,
  "notes": "Preference for short cut"
}

Response: 201 CREATED
{
  "success": true,
  "message": "Service booking created",
  "data": {
    "bookingId": 999,
    "serviceId": 5,
    "status": "CONFIRMED",
    "totalAmount": 300,
    "bookingDate": "2024-03-15T10:30:00Z"
  }
}
```

---

## Offer & Coupon APIs

### Create Offer
```http
POST /offer
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "storeId": 456,
  "name": "Summer Sale",
  "description": "20% off on all products",
  "discountType": "PERCENTAGE",  /* PERCENTAGE or FIXED_AMOUNT */
  "discountValue": 20,
  "minOrderAmount": 500,
  "maxDiscountCap": 2000,
  "validFrom": "2024-03-15",
  "validTo": "2024-03-31",
  "usageLimit": 100,
  "targetAudience": "ALL"  /* ALL, NEW_CUSTOMERS, VIP */
}

Response: 201 CREATED
{
  "success": true,
  "data": {
    "id": 200,
    "name": "Summer Sale",
    "status": "DRAFT",
    "discount": "20%",
    "usage": "0/100"
  }
}
```

### Generate Coupon Code
```http
POST /coupon/generate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "offerId": 200,
  "code": "SUMMER20",
  "quantity": 100,
  "minOrderAmount": 500
}

Response: 201 CREATED
{
  "success": true,
  "data": {
    "id": 301,
    "code": "SUMMER20",
    "offerId": 200,
    "quantity": 100,
    "usageCount": 0,
    "status": "ACTIVE"
  }
}
```

### Apply Coupon to Order
```http
POST /order/456/apply-coupon
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "couponCode": "SUMMER20"
}

Response: 200 OK
{
  "success": true,
  "message": "Coupon applied successfully",
  "data": {
    "orderId": 456,
    "couponCode": "SUMMER20",
    "discountAmount": 300,
    "newTotal": 1293,
    "message": "20% discount applied"
  }
}
```

### List Offers
```http
GET /offer?storeId=456&status=ACTIVE&page=1
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [ /* offers list */ ],
    "totalElements": 25
  }
}
```

---

## Approval Workflow APIs

### Get Pending Approvals
```http
GET /approval?status=PENDING&page=1&pageSize=20
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 500,
        "requestType": "PRODUCT_CREATION",
        "requestId": 1,
        "requester": "user@store.com",
        "status": "PENDING",
        "createdAt": "2024-03-14T10:00:00Z",
        "priority": "HIGH",
        "summary": "New product: Laptop - 5000"
      }
    ],
    "totalElements": 45
  }
}
```

### Approve Request
```http
POST /approval/500/approve
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "comments": "Looks good, approved"
}

Response: 200 OK
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    "id": 500,
    "status": "APPROVED",
    "approvedAt": "2024-03-14T11:00:00Z",
    "approvedBy": "admin@system.com"
  }
}
```

### Reject Request
```http
POST /approval/500/reject
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "reason": "Price too high, please revise",
  "comments": "Please review pricing strategy"
}

Response: 200 OK
{
  "success": true,
  "message": "Request rejected",
  "data": {
    "id": 500,
    "status": "REJECTED",
    "rejectionReason": "Price too high, please revise",
    "rejectedAt": "2024-03-14T11:00:00Z"
  }
}
```

---

## Business & Store APIs

### Get Business Details
```http
GET /business/123
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 123,
    "name": "My Business",
    "description": "Business description",
    "type": "RETAIL",
    "registrationNumber": "GST-123",
    "rating": 4.5,
    "status": "ACTIVE",
    "owner": "owner@business.com"
  }
}
```

### Get Store Details
```http
GET /store/456
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 456,
    "name": "Main Store",
    "businessId": 123,
    "address": "123 Main Street",
    "phone": "+91-9876543210",
    "email": "store@business.com",
    "operatingHours": {
      "monday": "09:00-22:00",
      "tuesday": "09:00-22:00"
    },
    "status": "ACTIVE",
    "rating": 4.6
  }
}
```

### Update Store Details
```http
PUT /store/456
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Main Store - Updated",
  "phone": "+91-9876543211",
  "operatingHours": {
    "monday": "08:00-23:00"
  }
}

Response: 200 OK
{
  "success": true,
  "message": "Store updated successfully",
  "data": { /* updated store */ }
}
```

---

## Analytics & Reporting APIs

### Get Store Analytics
```http
GET /analytics/store/456?startDate=2024-03-01&endDate=2024-03-31
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "period": "March 2024",
    "totalOrders": 250,
    "totalRevenue": 500000,
    "averageOrderValue": 2000,
    "totalCustomers": 180,
    "orderGrowth": 15.5,  /* percentage */
    "topProducts": [
      {
        "id": 1,
        "name": "Product 1",
        "sales": 50,
        "revenue": 50000
      }
    ],
    "dailyTrends": [
      {
        "date": "2024-03-14",
        "orders": 12,
        "revenue": 25000
      }
    ]
  }
}
```

### Get System Health Report
```http
GET /analytics/system-health
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "apiUptime": 99.99,
    "databaseLatency": 5,  /* ms */
    "errorRate": 0.01,  /* percentage */
    "activeUsers": 1250,
    "totalTransactions": 5000,
    "status": "HEALTHY"
  }
}
```

### Get Entity Overview
```http
GET /analytics/entity-overview
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "success": true,
  "data": {
    "totalBusinesses": 500,
    "totalStores": 1200,
    "totalProducts": 50000,
    "totalServices": 5000,
    "totalOrders": 100000,
    "totalRevenue": 50000000,
    "avgOrderValue": 500,
    "topCategories": [/* ... */]
  }
}
```

---

## Error Codes

| Code | Message | Cause | Solution |
|------|---------|-------|----------|
| 400 | Bad Request | Invalid request parameters | Check request format & parameters |
| 401 | Unauthorized | Missing/invalid JWT token | Provide valid JWT token in Authorization header |
| 403 | Forbidden | Insufficient permissions | Check user permissions for this action |
| 404 | Not Found | Resource not found | Verify resource ID is correct |
| 422 | Validation Error | Request data validation failed | Check error details & fix data |
| 429 | Too Many Requests | Rate limit exceeded | Wait before making new requests |
| 500 | Internal Server Error | Server error | Contact support with request ID |
| 503 | Service Unavailable | Service temporarily down | Retry after some time |

---

## Rate Limiting

### Rate Limits
- **Authenticated Users**: 1000 requests/hour
- **Public Endpoints**: 100 requests/hour
- **Payment APIs**: 50 requests/hour
- **Bulk Operations**: 10 requests/hour

### Rate Limit Headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1710511800
```

---

## Common Response Formats

### Success Response
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation successful",
  "timestamp": "2024-03-14T10:30:00Z",
  "data": { /* response data */ }
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

### Pagination Response
```json
{
  "success": true,
  "data": {
    "content": [ /* array of items */ ],
    "totalElements": 500,
    "totalPages": 25,
    "currentPage": 1,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Testing with Postman

1. **Import Collection**: `ClappN/postman/ClappN.postman_collection.json`
2. **Set Environment Variables**:
   - `base_url`: http://localhost:8080
   - `jwt_token`: Your authentication token
   - `store_id`: Your store ID
3. **Run Requests**: Use Postman runner for automated testing

---

**Last Updated**: March 14, 2026  
**API Version**: 2.0  
**Status**: ✅ Production Ready
