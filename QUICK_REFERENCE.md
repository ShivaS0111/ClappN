# Quick Reference Guide - Multimedia Features

## 🎯 Quick Start

### Adding Images to Products
```json
{
  "name": "Product Name",
  "desc": "Description",
  "businessTypeId": 1,
  "categories": [1, 2],
  "amount": 99.99,
  "currency": 1,
  "status": 1,
  "thumbnailUrl": "https://cdn.example.com/product-thumb.jpg",
  "galleryUrls": "[\"https://cdn.example.com/img1.jpg\",\"https://cdn.example.com/img2.jpg\"]"
}
```

### Adding Images to Services
```json
{
  "name": "Service Name",
  "desc": "Description",
  "businessTypeId": 1,
  "categoryIds": [1, 2],
  "amount": 149.99,
  "currency": 1,
  "duration": 60,
  "status": 1,
  "thumbnailUrl": "https://cdn.example.com/service-thumb.jpg",
  "galleryUrls": "[\"https://cdn.example.com/img1.jpg\"]"
}
```

### Adding Banner & Gallery to Business
```json
{
  "businessName": "Business Name",
  "description": "Description",
  "status": 1,
  "contact": "contact@business.com",
  "email": "business@email.com",
  "website": "https://business.com",
  "address": "123 Main St",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "bannerUrl": "https://cdn.example.com/business-banner.jpg",
  "galleryUrls": "[\"https://cdn.example.com/b1.jpg\",\"https://cdn.example.com/b2.jpg\"]"
}
```

### Adding Banner & Gallery to Store
```json
{
  "storeName": "Store Location",
  "description": "Description",
  "status": 1,
  "businessId": 1,
  "businessType": 1,
  "address": "456 Store Ave",
  "phone": "123-456-7890",
  "email": "store@email.com",
  "hours": "9 AM - 9 PM",
  "bannerUrl": "https://cdn.example.com/store-banner.jpg",
  "galleryUrls": "[\"https://cdn.example.com/s1.jpg\",\"https://cdn.example.com/s2.jpg\"]"
}
```

---

## 📊 Supported Entities

| Entity | Thumbnail | Gallery | Banner | Notes |
|--------|-----------|---------|--------|-------|
| Product | ✅ | ✅ | - | Main product image + gallery |
| Service | ✅ | ✅ | - | Service image + gallery |
| Store Product | ✅ | ✅ | - | Store-specific product images |
| Store Service | ✅ | ✅ | - | Store-specific service images |
| Business | - | ✅ | ✅ | Business banner + gallery |
| Store | - | ✅ | ✅ | Store banner + gallery |

---

## 🗂️ File Locations

### Entities
```
businesstype/infra/entity/
  - BusinessProductEntity.java (thumbnail, gallery)
  - BusinessServiceEntity.java (thumbnail, gallery)

businessstore/infra/entity/
  - StoreOfferedProductEntity.java (thumbnail, gallery)
  - StoreOfferedServiceEntity.java (thumbnail, gallery)
  - BusinessEntity.java (banner, gallery)
  - StoreEntity.java (banner, gallery)
```

### Domain Models
```
businesstype/domain/model/
  - BusinessProduct.java
  - BusinessService.java

businessstore/domain/model/
  - StoreOfferedProduct.java
  - StoreOfferedService.java
  - Business.java
  - Store.java
```

### DTOs
```
businesstype/api/dto/
  - BusinessProductDTO.java
  - BusinessServiceDTO.java

businessstore/api/dto/
  - StoreOfferedProductDTO.java
  - StoreOfferedServiceDTO.java
  - BusinessDTO.java
  - StoreDTO.java
```

### Requests
```
businesstype/api/request/
  - AddNewBusinessProductRequest.java
  - AddNewBusinessServiceRequest.java

businessstore/api/request/
  - AddNewStoreOfferedProductRequest.java
  - AddNewStoreOfferedServiceRequest.java
  - AddNewBusinessRequest.java
  - UpdateBusinessRequest.java
  - AddNewStoreRequest.java
```

### Mappers
```
businesstype/infra/mapper/
  - BusinessProductEntityMapper.java
  - BusinessServiceEntityMapper.java

businesstype/api/mapper/
  - BusinessProductDTOMapper.java
  - BusinessServiceDTOMapper.java

businessstore/infra/mapper/
  - StoreProductEntityMapper.java
  - StoreOfferedServiceEntityMapper.java
  - BusinessEntityMapper.java
  - StoreEntityMapper.java

businessstore/api/mapper/
  - StoreOfferedProductDTOMapper.java
  - StoreOfferedServiceDTOMapper.java
  - BusinessDTOMapper.java
  - StoreDTOMapper.java
```

---

## 🔌 API Endpoints Enhanced

### Business Product
- `POST /api/business-product` - Create with images
- `GET /api/business-product/{id}` - Get with images
- `PUT /api/business-product/{id}` - Update images

### Business Service
- `POST /api/business-service` - Create with images
- `GET /api/business-service/{id}` - Get with images
- `PUT /api/business-service/{id}` - Update images

### Store Product
- `POST /api/store-product` - Create with images
- `GET /api/store-product/{id}` - Get with images
- `PUT /api/store-product/{id}` - Update images

### Store Service
- `POST /api/store-service` - Create with images
- `GET /api/store-service/{id}` - Get with images
- `PUT /api/store-service/{id}` - Update images

### Business
- `POST /api/business` - Create with banner and gallery
- `GET /api/business/{id}` - Get with images
- `PUT /api/business/{id}` - Update images

### Store
- `POST /api/stores` - Create with banner and gallery
- `GET /api/stores/{id}` - Get with images
- `PUT /api/stores/{id}` - Update images

---

## 📋 Field Details

### Thumbnail URL
- **Type:** String (max 500 characters)
- **Format:** URL to image file
- **Nullable:** Yes
- **Example:** `https://cdn.example.com/product-thumb.jpg`

### Banner URL
- **Type:** String (max 500 characters)
- **Format:** URL to image file
- **Nullable:** Yes
- **Example:** `https://cdn.example.com/business-banner.jpg`

### Gallery URLs
- **Type:** String (JSON array)
- **Format:** JSON array of URL strings
- **Nullable:** Yes
- **Example:** `["https://cdn.example.com/1.jpg", "https://cdn.example.com/2.jpg"]`

---

## 🗄️ Database Schema

### Product/Service Tables
```sql
ALTER TABLE business_product ADD thumbnail_url VARCHAR(500);
ALTER TABLE business_product ADD gallery_urls JSON;
ALTER TABLE business_service ADD thumbnail_url VARCHAR(500);
ALTER TABLE business_service ADD gallery_urls JSON;
```

### Store Product/Service Tables
```sql
ALTER TABLE store_offered_product ADD thumbnail_url VARCHAR(500);
ALTER TABLE store_offered_product ADD gallery_urls JSON;
ALTER TABLE store_offered_service ADD thumbnail_url VARCHAR(500);
ALTER TABLE store_offered_service ADD gallery_urls JSON;
```

### Business/Store Tables
```sql
ALTER TABLE business ADD banner_url VARCHAR(500);
ALTER TABLE business ADD gallery_urls JSON;
ALTER TABLE store ADD banner_url VARCHAR(500);
ALTER TABLE store ADD gallery_urls JSON;
```

---

## ✅ Deployment Checklist

- [ ] Database migrations applied
- [ ] Application compiled successfully
- [ ] All tests passing
- [ ] Image fields can be created
- [ ] Image fields can be read
- [ ] Image fields can be updated
- [ ] Image fields can be set to null
- [ ] API responses include image fields
- [ ] Documentation reviewed
- [ ] Ready for production

---

## 🐛 Troubleshooting

### Images not saving
- Check URL format is valid
- Verify database migration was applied
- Check for null pointer exceptions in logs

### Images not returning in response
- Verify fields are not null in database
- Check mapper is configured correctly
- Ensure DTO includes image fields

### Gallery JSON invalid
- Verify JSON format: `["url1", "url2"]`
- Check for unescaped quotes in URLs
- Ensure URLs are valid strings

### Migration fails
- Verify database permissions
- Check if columns already exist
- Review error messages in logs

---

## 📚 Related Files

### Documentation
- `THUMBNAIL_GALLERY_IMPLEMENTATION.md` - Phase 1 details
- `BANNER_GALLERY_BUSINESS_STORE_IMPLEMENTATION.md` - Phase 2 details
- `COMPLETE_IMPLEMENTATION_SUMMARY.md` - Combined overview
- `EXECUTIVE_SUMMARY.md` - High-level summary
- `CHANGES_SUMMARY.md` - Detailed change list

### Migrations
- `V3__add_thumbnail_gallery_to_products_services.sql`
- `V4__add_banner_gallery_to_business_store.sql`

---

## 🔗 Quick Links

### API Documentation
- Product API: `/api/business-product`
- Service API: `/api/business-service`
- Store API: `/api/stores`
- Business API: `/api/business`

### Database
- Tables: 6 updated
- Columns: 12 added
- Migrations: 2 new

### Code
- Entities: 6 updated
- DTOs: 6 updated
- Mappers: 11 updated
- Requests: 7 updated

---

## 💡 Best Practices

1. **URL Format**
   - Use HTTPS URLs
   - Ensure URLs are accessible
   - Include file extension

2. **Gallery Organization**
   - Limit to 5-10 images per gallery
   - Use consistent image sizes
   - Follow naming convention

3. **Image Naming**
   - Use descriptive names
   - Include entity ID in filename
   - Use lowercase with hyphens

4. **Performance**
   - Use CDN for image storage
   - Optimize image sizes
   - Consider caching strategy

---

## 🎓 Implementation Notes

- All fields are **nullable** for backward compatibility
- JSON storage allows future enhancements
- Consistent mapper pattern applied
- No breaking changes to existing APIs
- Full CRUD support for all entities

---

**Last Updated:** 2026-03-14  
**Status:** ✅ Production Ready  
**Version:** 1.0

