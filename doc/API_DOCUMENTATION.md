# API Documentation

## Base URL

`http://localhost:8080`

---

## User API

### (No endpoints currently defined)

---

## Category API

### POST /api/categories/add
- **Description:** Create a new category
- **Sample Request:**
```json
{
  "name": "Retail",
  "parentId": 1
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "name": "Retail",
    "parentId": 1
  }
}
```

### GET /api/categories/search
- **Description:** Search categories by keyword
- **Sample Request:**
```json
{
  "keyword": "Retail"
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "name": "Retail",
      "parentId": 1
    }
  ]
}
```

### GET /api/categories/tree
- **Description:** Get category tree
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Root Category",
      "children": [ ... ]
    }
  ]
}
```

---

## Business Type API

### GET /business-type/list
- **Description:** List all business types
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "businessName": "Supermarket",
      "description": "Large retail store"
    }
  ]
}
```

### GET /business-type/search
- **Description:** Search business types by keyword
- **Sample Request:**
```json
{
  "keyword": "Supermarket"
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "businessName": "Supermarket",
      "description": "Large retail store"
    }
  ]
}
```

### POST /business-type/add
- **Description:** Add a new business type
- **Sample Request:**
```json
{
  "businessName": "Supermarket",
  "description": "Large retail store"
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "businessName": "Supermarket",
    "description": "Large retail store"
  }
}
```

---

## Business Service API

### GET /business-service/list
- **Description:** List all business services
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "serviceName": "Delivery"
    }
  ]
}
```

### POST /business-service/search
- **Description:** Search business services by keyword
- **Sample Request:**
```json
{
  "keyword": "Delivery"
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "serviceName": "Delivery"
    }
  ]
}
```

---

## Business Product API

### GET /business-product/list
- **Description:** List all business products
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "productName": "Milk"
    }
  ]
}
```

---

## Store Offered Service API

### GET /store-service/list/{storeId}
- **Description:** List services offered by a store (by storeId)
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "serviceName": "Delivery"
    }
  ]
}
```

---

## Store Item Price API

### POST /store-item/service-price/update
- **Description:** Update service price
- **Sample Request:**
```json
{
  "storeId": 1,
  "serviceId": 2,
  "price": 100.0
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "serviceId": 2,
    "price": 100.0
  }
}
```

### POST /store-item/product-lot-price/update
- **Description:** Update product lot price
- **Sample Request:**
```json
{
  "storeId": 1,
  "productId": 3,
  "price": 50.0
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "productId": 3,
    "price": 50.0
  }
}
```

### POST /store-item/product-price/update
- **Description:** Update product price
- **Sample Request:**
```json
{
  "storeId": 1,
  "productId": 3,
  "price": 45.0
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "productId": 3,
    "price": 45.0
  }
}
```

---

## Store API

### GET /stores/list
- **Description:** List all stores
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "storeName": "Main Street Store"
    }
  ]
}
```

---

## Business Entity API

### GET /business/list
- **Description:** List all businesses
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "businessName": "ABC Corp"
    }
  ]
}
```

### POST /business/search
- **Description:** Search businesses by keyword
- **Sample Request:**
```json
{
  "keyword": "ABC"
}
```
- **Sample Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "businessName": "ABC Corp"
    }
  ]
}
```

---

## Error Responses

- **400 Bad Request:** Invalid input.
- **404 Not Found:** Entity not found.
- **500 Internal Server Error:** Unexpected error.

---

## Notes

- All endpoints accept and return `application/json`.
- Authentication may be required for some endpoints.
