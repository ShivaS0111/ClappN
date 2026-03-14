# Banner and Gallery Feature Implementation for Business and Store

## Overview
Successfully added banner and gallery image functionality to Business and Store entities in the ClappN application.

## Database Changes

### Migration File: `V4__add_banner_gallery_to_business_store.sql`
**Location:** `src/main/resources/db/migration/`

Added the following columns to database tables:
- `banner_url` (VARCHAR(500)) - Single banner image URL
- `gallery_urls` (JSON) - Array of gallery image URLs stored as JSON

**Tables Updated:**
1. `business` - Business profile entity
2. `store` - Store profile entity

## Entity Layer Changes

### BusinessEntity
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/entity/BusinessEntity.java`
- Added `bannerUrl` field (String, column: `banner_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

### StoreEntity
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/entity/StoreEntity.java`
- Added `bannerUrl` field (String, column: `banner_url`)
- Added `galleryUrls` field (String, JSON column: `gallery_urls`)

## Domain Model Changes

### Business
**File:** `src/main/java/biz/craftline/server/feature/businessstore/domain/model/Business.java`
- Added `bannerUrl` field (String)
- Added `galleryUrls` field (String)

### Store
**File:** `src/main/java/biz/craftline/server/feature/businessstore/domain/model/Store.java`
- Added `bannerUrl` field (String)
- Added `galleryUrls` field (String)

## DTO Layer Changes

### BusinessDTO
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/dto/BusinessDTO.java`
- Added `bannerUrl` field
- Added `galleryUrls` field

### StoreDTO
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/dto/StoreDTO.java`
- Added `bannerUrl` field
- Added `galleryUrls` field

## Request Classes Changes

### AddNewBusinessRequest
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/request/AddNewBusinessRequest.java`
- Added `bannerUrl` field
- Added `galleryUrls` field

### UpdateBusinessRequest
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/request/UpdateBusinessRequest.java`
- Added `bannerUrl` field
- Added `galleryUrls` field

### AddNewStoreRequest
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/request/AddNewStoreRequest.java`
- Added `bannerUrl` field
- Added `galleryUrls` field

## Mapper Layer Changes

### BusinessEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/mapper/BusinessEntityMapper.java`
- Updated `toEntity()` to map banner and gallery fields
- Updated `toDomain()` to map banner and gallery fields

### StoreEntityMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/infra/mapper/StoreEntityMapper.java`
- Updated `toEntity()` to map banner and gallery fields
- Updated `toDomain()` to map banner and gallery fields

### BusinessDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/mapper/BusinessDTOMapper.java`
- Updated `toDTO()` to include banner and gallery in DTO conversion
- Updated `toDomain(BusinessDTO)` to include banner and gallery in domain conversion
- Updated `toDomain(AddNewBusinessRequest)` to include banner and gallery fields
- Updated `toDomain(UpdateBusinessRequest)` to include banner and gallery fields
- Updated `toUpdated()` to handle banner and gallery updates

### StoreDTOMapper
**File:** `src/main/java/biz/craftline/server/feature/businessstore/api/mapper/StoreDTOMapper.java`
- Updated `toDTO()` to include banner and gallery in DTO conversion
- Updated `toDomain(StoreDTO)` to include banner and gallery in domain conversion
- Updated `toDomain(AddNewStoreRequest)` to include banner and gallery fields

## Data Structure

### Banner URL (bannerUrl)
- Single image URL (VARCHAR 500 bytes max)
- Used for business/store profile banner/hero image
- Example: `https://cdn.example.com/business/business-001-banner.jpg`

### Gallery URLs (galleryUrls)
- JSON array format for multiple images
- Stored in database as JSON column
- Used for business/store photo gallery
- Recommended format:
  ```json
  [
    "https://cdn.example.com/business/business-001-1.jpg",
    "https://cdn.example.com/business/business-001-2.jpg",
    "https://cdn.example.com/business/business-001-3.jpg"
  ]
  ```

## API Usage Examples

### Creating a Business with Banner and Gallery

```json
{
  "businessName": "My Business",
  "description": "Business Description",
  "status": 1,
  "contact": "contact@business.com",
  "email": "business@email.com",
  "website": "https://business.com",
  "address": "123 Main Street",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "bannerUrl": "https://cdn.example.com/business/banner.jpg",
  "galleryUrls": "[\"https://cdn.example.com/business/1.jpg\",\"https://cdn.example.com/business/2.jpg\"]"
}
```

### Updating a Business with Banner and Gallery

```json
{
  "id": 1,
  "businessName": "My Business Updated",
  "description": "Updated Description",
  "status": 1,
  "contact": "contact@business.com",
  "email": "business@email.com",
  "website": "https://business.com",
  "address": "123 Main Street",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "bannerUrl": "https://cdn.example.com/business/banner-updated.jpg",
  "galleryUrls": "[\"https://cdn.example.com/business/1.jpg\",\"https://cdn.example.com/business/2.jpg\",\"https://cdn.example.com/business/3.jpg\"]"
}
```

### Creating a Store with Banner and Gallery

```json
{
  "storeName": "Store Location",
  "description": "Store Description",
  "status": 1,
  "businessId": 1,
  "businessType": 1,
  "address": "456 Store Ave",
  "addressId": 1,
  "phone": "123-456-7890",
  "email": "store@email.com",
  "hours": "9 AM - 9 PM",
  "bannerUrl": "https://cdn.example.com/store/banner.jpg",
  "galleryUrls": "[\"https://cdn.example.com/store/1.jpg\",\"https://cdn.example.com/store/2.jpg\"]"
}
```

## Migration Steps

1. **Apply Database Migration**
   - Run the migration file `V4__add_banner_gallery_to_business_store.sql`
   - This will add the new columns to business and store tables

2. **Rebuild Application**
   - Compile the application with the new entity and mapper changes
   - All endpoints will automatically support banner and gallery fields

3. **Update Existing Records (Optional)**
   - Use UPDATE SQL queries to populate existing records with image URLs
   - Or leave them null if no images are available

## Backward Compatibility

- All banner and gallery fields are **nullable**
- Existing APIs continue to work without providing these fields
- Fields will be `null` in responses for records that don't have images
- No breaking changes to existing functionality

## Technical Notes

- Gallery URLs are stored as JSON in the database for flexibility
- String representation is used in Java entities (can be parsed/serialized as needed)
- Consider using a library like Jackson for JSON serialization if enhanced handling is needed
- Maximum banner URL length: 500 characters
- Gallery can contain unlimited images (limited by JSON/database column size)

## Feature Details

### Business Banner & Gallery
- Used for business profile display
- Banner: Large hero/header image for business profile page
- Gallery: Multiple photos showcasing business facilities, products, or team
- Visible in business profile and search results

### Store Banner & Gallery
- Used for individual store location profile
- Banner: Store location banner/hero image
- Gallery: Photos of store location, interior, products
- Visible in store details page
- Can be different from parent business images for store-specific customization

## Future Enhancements

1. Add image upload endpoints with validation
2. Implement image transformation and compression
3. Add CDN integration for image storage
4. Implement multiple image size variants (thumbnail, medium, large)
5. Add gallery ordering/sorting capabilities
6. Implement image deletion and management endpoints
7. Add image metadata (title, description, alt text)
8. Implement image cropping and editing functionality

## Compilation Status

✅ **All files compile successfully** with no critical errors.
⚠️ Minor: 1 unused import warning in BusinessEntity.java (non-critical)

## Summary

✅ **Feature Status:** COMPLETE AND READY FOR DEPLOYMENT

Both Business and Store entities now support:
- Banner image URLs
- Multiple gallery images (stored as JSON)
- Full CRUD operations
- Seamless API integration
- Backward compatibility

The implementation follows the existing application architecture and patterns, ensuring consistency and maintainability.

