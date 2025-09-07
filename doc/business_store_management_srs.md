# Business & Store Product/Service Management Platform - SRS

## 1. Introduction

### 1.1 Purpose
The platform manages businesses and stores, their products, services, and packages, with approval workflows, inventory management, pricing, role-based access, and reporting features.

### 1.2 Scope
- Manage Business Types and hierarchical Categories for products/services.
- Businesses can manage stores, products, services, and packages.
- Store-level inventory, pricing, and availability management.
- Role-based access control for system, business, and store levels.
- Approval workflows for products, services, and packages.
- Audit logging, analytics, and search/filtering capabilities.

## 2. Core Entities

| Entity | Purpose | Attributes | Functionality |
|--------|---------|-----------|---------------|
| Business Types | Define business domains | id, name, description, parentId, status | Determines allowed categories, packages, and store/business offerings |
| Categories | Multi-level hierarchy for products/services | id, name, parentId, type (product/service), status | Supports filtering, validation, unisex/general categories |
| Businesses | Business-level operations | id, name, description, businessTypeIds, status, ownerId | Can offer products/services, create packages |
| Stores | Store-level operations | id, businessId, name, address, operationalHours, status | Manage inventory, pricing, packages, store-specific offerings |
| Products | Items for sale | id, name, description, categories, SKU, status | Supports approval workflow, lots, stock, price history |
| Services | Non-tangible offerings | id, name, description, categories, status | Can belong to store/business, supports approval workflow, price history, packages |
| Packages | Bundles of products/services | id, name, description, type (product/service/mixed), storeId/businessId, status | Fixed or dynamic pricing, optional approval workflow |
| Users | Platform users | id, name, email/phone, roleId, storeId/businessId, status | JWT/session authentication, role-based access |
| Roles & Permissions | Access control | id, level (system/business/store), permissions | Defines what actions a user can perform |
| Approval Workflow | Product/service/package approval | id, entityType, entityId, status, approvedBy, timestamp | Tracks Draft → Pending → Approved → Live/Inactive |
| Inventory & Availability | Product stock tracking | productLotId, storeId, quantity, expiryDate, lowStockFlag | Multi-store inventory, lot-based availability |
| Pricing Management | Product/service/package pricing | entityId, price, effectiveDate | Maintains price history and dynamic package pricing |
| Audit & Logs | Track changes | id, entityType, entityId, userId, action, timestamp | Complete history of modifications |
| Reporting & Analytics | Data insights | businessId, storeId, categoryId, filters | Sales, inventory, package performance |
| Search & Filtering | Product/service discovery | category, business, store, price range, availability | Distinguish business vs store offerings |

## 3. Functional Requirements

### 3.1 Business and Store Management
- CRUD operations for Businesses and Stores.
- Assign stores to businesses.
- Assign business types to businesses.
- Operational hours per store.

### 3.2 Product Management
- CRUD operations for Products.
- Assign categories and SKUs.
- Maintain product lots for inventory and pricing.
- Product status workflow: Draft → Pending → Approved → Live/Inactive.
- Approval required for live products.

### 3.3 Service Management
- CRUD operations for Services.
- Assign categories.
- Maintain store/business-level pricing.
- Approval workflow for services.
- Include services in packages.

### 3.4 Package Management
- Create bundles of products and/or services.
- Support fixed or dynamic pricing.
- Optional approval workflow.
- Belong to business or store.

### 3.5 Inventory Management
- Track product lots per store.
- Manage stock levels, lot expiry.
- Multi-store support.
- Low stock notifications.

### 3.6 Pricing Management
- Maintain price history for products/services.
- Lot-based pricing for products.
- Store/business-level pricing for services.
- Dynamic pricing for packages (sum of items).

### 3.7 Roles & Permissions
- **System Level:** Admin, Moderator – full access, approve content, manage businesses/stores.
- **Business Level:** Owner/Admin, Manager, Staff – manage stores, approve products/services/packages.
- **Store Level:** Admin, Manager, Staff – manage store-specific offerings, inventory, pricing.

### 3.8 Approval Workflow
- Applies to products, services, packages.
- Status: Draft → Pending → Approved → Live/Inactive.
- Approval roles: System Admin, System Moderator, Business Owner/Admin.
- Audit trail for every approval.

### 3.9 User Management
- JWT/session-based authentication.
- Assign roles at system, business, store levels.
- Role-based access enforcement.

### 3.10 Audit & Logs
- Track all entity changes.
- Record user, timestamp, action.

### 3.11 Reporting & Analytics
- Filter by business/store offerings.
- Sales, inventory, package performance.
- Filter by business type, category, store.

### 3.12 Search & Filtering
- By category, business, store.
- By product/service name, price range, availability.
- Distinguish between business-offered and store-offered.

## 4. Database Tables

| Table | Description |
|-------|------------|
| Users | System, business, store users |
| Roles | System/business/store roles |
| BusinessTypes | Define business domains |
| Businesses | Business info, type(s) |
| Stores | Store info under businesses |
| Categories | Multi-level categories for products/services |
| Products | Product info |
| ProductLots | Stock & pricing per lot |
| StoreProducts | Products offered by stores |
| Services | Service info |
| StoreServices | Services offered by stores |
| PriceHistory | Track changes for products/services pricing |
| Packages | Bundles of products/services |
| PackageItems | Items inside packages |
| Approvals | Approval workflow tracking |
| AuditLogs | Track entity changes |
| Promotions | Discounts/offers for products/services/packages |

## 5. Non-Functional Requirements
- **Security:** JWT authentication, encrypted passwords, role-based authorization.
- **Scalability:** Support multiple businesses, stores, and high volume of products.
- **Performance:** Low latency for search, filtering, and inventory updates.
- **Reliability:** Ensure data consistency for inventory and pricing.
- **Auditability:** Full logs for compliance and troubleshooting.
- **Extensibility:** Easy addition of new business types, categories, or services.

## 6. Workflows

### 6.1 Product/Service Approval Workflow
1. Draft → Creator/Manager saves entity.
2. Pending Approval → Submitted to approver.
3. Approved → Entity goes live.
4. Live/Inactive → Entity availability changes over time.

### 6.2 Inventory Workflow
1. Product lot created with quantity, price, expiry.
2. Stock changes updated per transaction.
3. Low stock triggers notification.
4. Expired lots marked unavailable.

## 7. UML Class Diagram

```
BusinessType
  id
  name
  description
  parentId
  status

Category
  id
  name
  parentId
  type
  status

Business
  id
  name
  description
  businessTypeIds
  ownerId
  status

Store
  id
  businessId
  name
  address
  operationalHours
  status

Product
  id
  name
  description
  categories
  SKU
  status

Service
  id
  name
  description
  categories
  status

Package
  id
  name
  description
  type
  storeId/businessId
  status

User
  id
  name
  email/phone
  roleId
  storeId/businessId
  status

Role
  id
  level
  permissions

Approval
  id
  entityType
  entityId
  status
  approvedBy
  timestamp

ProductLot
  id
  productId
  storeId
  quantity
  price
  expiryDate
```

## 8. ERD (High-Level Relationships)
- BusinessType → Category (1:N)
- Business → Store (1:N)
- Business → Product/Service (1:N)
- Store → StoreProduct/StoreService (1:N)
- Product → ProductLot (1:N)
- Package → PackageItems (1:N)
- User → Role (N:1)
- Approvals linked to Products/Services/Packages

