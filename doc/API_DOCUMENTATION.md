# API Documentation

## Brand APIs

### List Brands
- **GET** `/api/brands`
- **Description:** Returns all brands.
- **Response:**
```json
[
  {
    "id": 1,
    "name": "Brand A",
    "description": "Description of Brand A"
  }
]
```

### Get Brand by ID
- **GET** `/api/brands/{id}`
- **Description:** Returns a brand by ID.
- **Response:**
```json
{
  "id": 1,
  "name": "Brand A",
  "description": "Description of Brand A"
}
```

### Add Brand
- **POST** `/api/brands`
- **Description:** Adds a new brand.
- **Request:**
```json
{
  "name": "Brand A",
  "description": "Description of Brand A"
}
```
- **Response:**
```json
{
  "id": 1,
  "name": "Brand A",
  "description": "Description of Brand A"
}
```

### Delete Brand
- **DELETE** `/api/brands/{id}`
- **Description:** Deletes a brand by ID.

---

## Category APIs

### Add Category
- **POST** `/api/categories/add`
- **Request:**
```json
{
  "name": "Shoes",
  "parentId": 2
}
```
- **Response:**
```json
{
  "id": 3,
  "name": "Shoes",
  "parentId": 2
}
```

### Search Categories
- **GET** `/api/categories/search`
- **Request:**
```json
{
  "keyword": "Shoes"
}
```
- **Response:**
```json
[
  {
    "id": 3,
    "name": "Shoes",
    "parentId": 2
  }
]
```

### Get Category Tree
- **GET** `/api/categories/tree`
- **Response:**
```json
[
  {
    "id": 1,
    "name": "Men",
    "children": [
      {
        "id": 2,
        "name": "Shoes"
      }
    ]
  }
]
```

### Get Category Path
- **GET** `/api/categories/{id}/path`
- **Response:**
```json
[
  {
    "id": 1,
    "name": "Men"
  },
  {
    "id": 2,
    "name": "Shoes"
  }
]
```

---

## BusinessType APIs

### List Business Types
- **GET** `/business-type/list`
- **Response:**
```json
[
  {
    "id": 1,
    "businessName": "Retail",
    "description": "Retail business type"
  }
]
```

### Search Business Types
- **GET** `/business-type/search`
- **Request:**
```json
{
  "keyword": "Retail"
}
```
- **Response:**
```json
[
  {
    "id": 1,
    "businessName": "Retail",
    "description": "Retail business type"
  }
]
```

### Add Business Type
- **POST** `/business-type/add`
- **Request:**
```json
{
  "businessName": "Retail",
  "description": "Retail business type"
}
```
- **Response:**
```json
{
  "id": 1,
  "businessName": "Retail",
  "description": "Retail business type"
}
```

---

## BusinessService APIs

### List Business Services
- **GET** `/business-service/list`
- **Response:**
```json
[
  {
    "id": 1,
    "serviceName": "Delivery",
    "description": "Delivery service"
  }
]
```

### Search Business Services
- **POST** `/business-service/search`
- **Request:**
```json
{
  "keyword": "Delivery"
}
```
- **Response:**
```json
[
  {
    "id": 1,
    "serviceName": "Delivery",
    "description": "Delivery service"
  }
]
```

---

## BusinessProduct APIs

### List Business Products
- **GET** `/business-product/list`
- **Response:**
```json
[
  {
    "id": 1,
    "productName": "Laptop",
    "description": "Electronics"
  }
]
```

### Search Business Products
- **POST** `/business-product/search`
- **Request:**
```json
{
  "keyword": "Laptop"
}
```
- **Response:**
```json
[
  {
    "id": 1,
    "productName": "Laptop",
    "description": "Electronics"
  }
]
```

---

## BusinessEntity APIs

### List Businesses
- **GET** `/business/list`
- **Response:**
```json
[
  {
    "id": 1,
    "name": "ABC Retail",
    "type": "Retail"
  }
]
```

### Search Businesses
- **POST** `/business/search`
- **Request:**
```json
{
  "keyword": "ABC"
}
```
- **Response:**
```json
[
  {
    "id": 1,
    "name": "ABC Retail",
    "type": "Retail"
  }
]
```

---

## Store APIs

### List Stores
- **GET** `/stores/list`
- **Response:**
```json
[
  {
    "id": 1,
    "name": "Store 1",
    "businessId": 1
  }
]
```

### List Stores by Business ID
- **GET** `/stores/list/{businessId}`
- **Response:**
```json
[
  {
    "id": 1,
    "name": "Store 1",
    "businessId": 1
  }
]
```

---

## Notes
- All responses are wrapped in an `APIResponse` object.
- Error responses follow the same structure with an error message and code.
- For full request/response models, refer to the DTOs in the source code.
- Authentication and authorization are required for some endpoints (see SRS for details).

