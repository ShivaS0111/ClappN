# Thumbnail and Gallery Feature Implementation

## Overview
This document outlines the implementation of thumbnail and gallery image functionality for products and services in the ClappN application.

## Database Changes

### Migration File: `V3__add_thumbnail_gallery_to_products_services.sql`
**Location:** `src/main/resources/db/migration/`

Added the following columns to database tables:
- `thumbnail_url` (VARCHAR(500)) - Single thumbnail image URL
- `gallery_urls` (JSON) - Array of gallery image URLs stored as JSON

**Tables Updated:**
1. `business_product` - Main product definitions
2. `business_service` - Main service definitions
3. `store_offered_product` - Store-specific product offerings
4. `store_offered_service` - Store-specific service offerings

## Entity Layer Changes

### BusinessProductEntity
**File:** `src/main/java/biz/craftline/server/feature/businesstype/infra/entity/BusinessProductEntity.java`
- Added `thumbnailUrl` field (String, column: `thumbnail_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

### BusinessServiceEntity
**File:** `src/main/java/biz/craftline/server/feature/businesstype/infra/entity/BusinessServiceEntity.java`
- Added `thumbnailUrl` field (String, column: `thumbnail_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

### StoreOfferedProductEntity
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/entity/StoreOfferedProductEntity.java`
- Added `thumbnailUrl` field (String, column: `thumbnail_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

### StoreOfferedServiceEntity
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/entity/StoreOfferedServiceEntity.java`
- Added `thumbnailUrl` field (String, column: `thumbnail_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

## Domain Model Changes

### BusinessProduct
**File:** `src/main/java/biz/craftline/server/feature/businesstype/domain/model/BusinessProduct.java`
- Added `thumbnailUrl` field (String)
- Added `galleryUrls` field (String)

### BusinessService
**File:** `src/main/java/biz/craftline/server/feature/businesstype/domain/model/BusinessService.java`
- Added `thumbnailUrl` field (String)
- Added `galleryUrls` field (String)

### StoreOfferedProduct
**File:** `src/main/java/biz/craftline/server/feature/businessstore/domain/model/StoreOfferedProduct.java`
- Added `thumbnailUrl` field (String)
- Added `galleryUrls` field (String)

### StoreOfferedService
**File:** `src/main/java/biz/craftline/server/feature/businessstore/domain/model/StoreOfferedService.java`
- Added `thumbnailUrl` field (String)
- Added `galleryUrls` field (String)

## DTO Layer Changes

### BusinessProductDTO
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/dto/BusinessProductDTO.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### BusinessServiceDTO
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/dto/BusinessServiceDTO.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### StoreOfferedProductDTO
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/dto/StoreOfferedProductDTO.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### StoreOfferedServiceDTO
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/dto/StoreOfferedServiceDTO.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

## Request Classes Changes

### AddNewBusinessProductRequest
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/request/AddNewBusinessProductRequest.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### AddNewBusinessServiceRequest
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/request/AddNewBusinessServiceRequest.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### AddNewStoreOfferedProductRequest
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/request/AddNewStoreOfferedProductRequest.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

### AddNewStoreOfferedServiceRequest
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/request/AddNewStoreOfferedServiceRequest.java`
- Added `thumbnailUrl` field
- Added `galleryUrls` field

## Mapper Layer Changes

### BusinessProductEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businesstype/infra/mapper/BusinessProductEntityMapper.java`
- Updated `toEntity()` to map thumbnail and gallery fields
- Updated `toDomain()` to map thumbnail and gallery fields

### BusinessServiceEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businesstype/infra/mapper/BusinessServiceEntityMapper.java`
- Updated `toEntity()` to map thumbnail and gallery fields
- Updated `toDomain()` to map thumbnail and gallery fields
- Updated `toUpdate()` to handle thumbnail and gallery updates

### BusinessProductDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/mapper/BusinessProductDTOMapper.java`
- Updated `toDTO()` to include thumbnail and gallery in DTO conversion
- Updated `toDomain(BusinessProductDTO)` to include thumbnail and gallery in domain conversion
- Updated `toDomain(AddNewBusinessProductRequest)` to include thumbnail and gallery fields

### BusinessServiceDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businesstype/api/mapper/BusinessServiceDTOMapper.java`
- Updated `toDTO()` to include thumbnail and gallery in DTO conversion
- Updated `toDomain(BusinessServiceDTO)` to include thumbnail and gallery in domain conversion
- Updated `toDomain(AddNewBusinessServiceRequest)` to include thumbnail and gallery fields

### StoreProductEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/mapper/StoreProductEntityMapper.java`
- Updated `toDomain()` to map thumbnail and gallery fields
- Updated `toEntity()` to map thumbnail and gallery fields

### StoreOfferedServiceEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/mapper/StoreOfferedServiceEntityMapper.java`
- Updated `toDomain()` to map thumbnail and gallery fields
- Updated `toEntity()` to map thumbnail and gallery fields

### StoreOfferedProductDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/mapper/StoreOfferedProductDTOMapper.java`
- Updated `toDTO()` to include thumbnail and gallery in DTO conversion
- Updated `toDomain(AddNewStoreOfferedProductRequest)` to include thumbnail and gallery fields

### StoreOfferedServiceDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/mapper/StoreOfferedServiceDTOMapper.java`
- Updated `toDomain(StoreOfferedServiceDTO)` to include thumbnail and gallery fields
- Updated `toDTO()` to include thumbnail and gallery in DTO conversion
- Updated `toDomain(AddNewStoreOfferedServiceRequest)` to include thumbnail and gallery fields

## Data Structure

### Thumbnail URL (thumbnailUrl)
- Single image URL (VARCHAR 500 bytes max)
- Recommended for primary product/service image display
- Example: `https://cdn.example.com/products/product-001-thumb.jpg`

### Gallery URLs (galleryUrls)
- JSON array format for multiple images
- Stored in database as JSON column
- Recommended format:
  ```json
  [
    "https://cdn.example.com/products/product-001-1.jpg",
    "https://cdn.example.com/products/product-001-2.jpg",
    "https://cdn.example.com/products/product-001-3.jpg"
  ]
  ```

## API Usage Examples

### Creating a Product with Thumbnail and Gallery

```json
{
  "name": "Product Name",
  "desc": "Product Description",
  "businessTypeId": 1,
  "brandId": 1,
  "categories": [1, 2],
  "amount": 99.99,
  "currency": 1,
  "status": 1,
  "thumbnailUrl": "https://cdn.example.com/products/product-001-thumb.jpg",
  "galleryUrls": "[\"https://cdn.example.com/products/product-001-1.jpg\",\"https://cdn.example.com/products/product-001-2.jpg\"]"
}
```

### Creating a Service with Thumbnail and Gallery

```json
{
  "name": "Service Name",
  "desc": "Service Description",
  "businessTypeId": 1,
  "categoryIds": [1, 2],
  "amount": 149.99,
  "currency": 1,
  "duration": 60,
  "status": 1,
  "thumbnailUrl": "https://cdn.example.com/services/service-001-thumb.jpg",
  "galleryUrls": "[\"https://cdn.example.com/services/service-001-1.jpg\",\"https://cdn.example.com/services/service-001-2.jpg\"]"
}
```

## Migration Steps

1. **Apply Database Migration**
   - Run the migration file `V3__add_thumbnail_gallery_to_products_services.sql`
   - This will add the new columns to all four tables

2. **Rebuild Application**
   - Compile the application with the new entity and mapper changes
   - All endpoints will automatically support thumbnail and gallery fields

3. **Update Existing Records (Optional)**
   - Use UPDATE SQL queries to populate existing records with image URLs
   - Or leave them null if no images are available

## Backward Compatibility

- All thumbnail and gallery fields are **nullable**
- Existing APIs will continue to work without providing these fields
- Fields will be `null` in responses for records that don't have images

## Technical Notes

- Gallery URLs are stored as JSON in the database for flexibility
- String representation is used in Java entities (can be parsed/serialized as needed)
- Consider using a library like Jackson for JSON serialization if enhanced handling is needed
- Maximum thumbnail URL length: 500 characters
- Gallery can contain unlimited images (limited by JSON/database column size)

## Future Enhancements

1. Add image upload endpoints
2. Implement image validation and transformation
3. Add CDN integration for image storage
4. Implement image compression and multiple size variants
5. Add gallery ordering/sorting capabilities
6. Implement image deletion endpoints

