# Database Seed Configuration Documentation

## Overview

The `DataSeedConfig` class is a Spring Boot configuration component that automatically seeds the database with initial data when the application starts. This is useful for:

- Development and testing environments
- Initial application setup
- Ensuring consistent baseline data across instances
- Providing reference data for business types, categories, permissions, and roles

## Location

```
src/main/java/biz/craftline/server/config/DataSeedConfig.java
```

## Features

### 1. **Business Types Seeding**
Seeds 85+ business types including:
- **Retail & Commerce**: Retail Store, E-Commerce Platform, Supermarket, Shopping Mall, Department Store
- **Food & Beverage**: Restaurant, Cafe, Fast Food, Bakery, Pizzeria, Ice Cream Parlor, etc.
- **Technology & IT**: IT Services, Software Development, Web Design, Digital Marketing, etc.
- **Construction & Real Estate**: Construction, Real Estate, Property Management, Interior Design, etc.
- **Healthcare & Wellness**: Healthcare Clinic, Hospital, Dental, Opticals, Pharmacy, Fitness, etc.
- **Education & Training**: School, College, Coaching Center, Language Institute, Music Academy, etc.
- **Transportation & Logistics**: Taxi, Courier, Shipping, Car Rental, etc.
- **Financial Services**: Bank, Insurance, Investment Firm, Accounting, etc.
- **Entertainment & Media**: Movie Theater, Music Production, Gaming Studio, Broadcasting, etc.
- **Service & Maintenance**: Mechanic Services, Plumbing, Electrical, HVAC, Cleaning, etc.

### 2. **Categories Seeding**
Seeds a hierarchical category structure with:
- 20+ top-level categories
- Multiple subcategories with parent-child relationships
- Example hierarchy:
  ```
  Electronics (Parent)
  ├─ Mobiles
  ├─ Laptops
  ├─ Cameras
  ├─ Audio Devices
  └─ Wearables
  
  Home & Kitchen (Parent)
  ├─ Furniture
  │  ├─ Sofas
  │  ├─ Beds
  │  └─ Chairs
  ├─ Decor
  └─ Kitchenware
  ```

### 3. **Permissions Seeding**
Seeds 40+ permissions across multiple domains:
- **Store Management**: create, read, update, delete, metrics, settings
- **Product Management**: create, read, update, delete, inventory
- **Service Management**: create, read, update, delete
- **Business Management**: create, read, update, delete
- **Category Management**: create, read, update, delete
- **User Management**: create, read, update, delete
- **Order Management**: create, read, update, cancel, refund
- **Finance**: view, reports, pricing, coupons
- **Marketing**: campaigns, analytics, packages
- **Reports**: sales, inventory, user, system
- **Security & System**: monitor, access, audit, logs, settings, backup, maintenance

### 4. **Roles Seeding**
Seeds 35+ roles including:
- **System Roles**: SystemAdmin, Guest
- **Business Roles**: BusinessOwner, BusinessAdmin, BusinessManager
- **Store Roles**: StoreOwner, StoreManager, AssistantManager, ShiftSupervisor
- **Sales Roles**: SalesAssociate, Cashier, SalesManager
- **Inventory Roles**: InventoryStaff, StockKeeper, InventoryManager
- **Service Roles**: CustomerServiceRep, CustomerService, MaintenanceStaff
- **Vendor Roles**: VendorAdmin, VendorManager
- **Specialized Roles**: FinanceManager, MarketingManager, SecurityOfficer, AnalyticsManager, etc.

### 5. **Role-Permission Mapping**
Currently assigns all permissions to the SystemAdmin role. This can be expanded to define specific permissions for each role.

## How It Works

The seed operation is triggered automatically on application startup via the `CommandLineRunner` bean:

1. **Application Starts** → Spring Boot initializes beans
2. **DataSeedConfig Bean Created** → Configuration is loaded
3. **CommandLineRunner Executes** → `seedDatabase()` method runs
4. **Count Check** → Verifies if data already exists in the database
   - If business_type table is empty: Seed all data
   - If business_type table has data: Skip seeding (idempotent)
5. **Data Insertion** → Inserts data in this order:
   - Business Types
   - Categories (with hierarchical structure)
   - Permissions
   - Roles
   - Role-Permission Mappings
6. **Logging** → Progress and completion messages logged

## Database Requirements

Ensure these tables exist before seeding:
- `business_type`
- `categories`
- `permission`
- `role`
- `role_permission`

These should be created via Flyway migrations in:
```
src/main/resources/db/migration/
```

## Configuration

### Enable/Disable Seeding

To disable seeding in production, add to `application-prod.properties`:

```properties
app.seed.enabled=false
```

Then modify the `DataSeedConfig` class:

```java
@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeedConfig {
    // ...
}
```

### Idempotent Behavior

The seed operation is **idempotent** by default:
- Checks if `business_type` table is empty
- Only seeds if no data exists
- Safe to run multiple times without duplicates

## Extension Guide

### Adding New Business Types

```java
private void seedBusinessTypes() {
    // ...existing types...
    businessTypes.add(createBusinessType(
        86L,
        "My New Business Type",
        "Description of my new business type",
        1  // status: 1=active, 0=inactive
    ));
    // ...
}
```

### Adding New Categories

```java
private void addSubcategories(Map<Long, CategoryEntity> categoryMap, Map<Long, Long> parentMap) {
    // ...existing categories...
    createSubcategory(
        199L,           // id
        "New Category", // name
        1L,            // parentId (1 = Electronics)
        categoryMap
    );
    // ...
}
```

### Adding New Permissions

```java
private void seedPermissions() {
    // ...existing permissions...
    permissions.add(new PermissionEntity("my.new.permission"));
    permissionRepository.saveAll(permissions);
    // ...
}
```

### Adding New Roles

```java
private void seedRoles() {
    List<RoleEntity> roles = new ArrayList<>();
    // ...existing roles...
    
    RoleEntity newRole = new RoleEntity();
    newRole.setName("MyNewRole");
    newRole.setPermissions(new HashSet<>());
    roles.add(newRole);
    
    roleRepository.saveAll(roles);
    // ...
}
```

### Advanced: Custom Role-Permission Assignments

Replace the simplified `seedRolePermissions()` with specific mappings:

```java
private void seedRolePermissions() {
    RoleEntity storeManager = roleRepository.findByName("StoreManager");
    RoleEntity cashier = roleRepository.findByName("Cashier");
    
    Set<PermissionEntity> managerPerms = new HashSet<>();
    managerPerms.add(permissionRepository.findByName("store.read").orElse(null));
    managerPerms.add(permissionRepository.findByName("store.update").orElse(null));
    managerPerms.add(permissionRepository.findByName("product.read").orElse(null));
    storeManager.setPermissions(managerPerms);
    
    Set<PermissionEntity> cashierPerms = new HashSet<>();
    cashierPerms.add(permissionRepository.findByName("order.create").orElse(null));
    cashierPerms.add(permissionRepository.findByName("order.read").orElse(null));
    cashier.setPermissions(cashierPerms);
    
    roleRepository.saveAll(Arrays.asList(storeManager, cashier));
}
```

## Logging Output

During application startup, you'll see:

```
[INFO] Starting database seed...
[INFO] Seeding Business Types...
[INFO] Successfully seeded 85 business types
[INFO] Seeding Categories...
[INFO] Successfully seeded categories
[INFO] Seeding Permissions...
[INFO] Successfully seeded 42 permissions
[INFO] Seeding Roles...
[INFO] Successfully seeded 35 roles
[INFO] Seeding Role-Permission mappings...
[INFO] Assigned 42 permissions to SystemAdmin role
[INFO] Database seed completed successfully!
```

Or if data already exists:
```
[INFO] Database already populated. Skipping seed.
```

## Dependencies

The DataSeedConfig uses the following Spring Boot features:
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.context.annotation.Configuration`
- `org.springframework.boot.CommandLineRunner`
- `lombok` for logging (`@Slf4j`)

## Performance Notes

- Initial seed may take 5-30 seconds depending on system performance
- Idempotent design means subsequent application starts are instant
- No impact on production environments (seed only runs if table is empty)

## Troubleshooting

### Issue: Seed runs every time

**Cause**: Business type check might fail if table structure is wrong

**Solution**: Verify table creation via Flyway migrations

### Issue: Duplicate key errors

**Cause**: Seed ran multiple times and data wasn't cleaned

**Solution**: 
1. Delete seed data from database tables
2. Restart application
3. Or modify IDs to avoid conflicts

### Issue: Relationships not working

**Cause**: Parent-child category relationships failing

**Solution**: Ensure `CategoryEntity` with correct parent setup is inserted first

## Database Schema Expectations

```sql
CREATE TABLE business_type (
    id BIGINT PRIMARY KEY,
    business_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 0,
    created_by BIGINT,
    created_at DATE,
    updated_at DATE
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    parent_id BIGINT,
    status INTEGER DEFAULT 1,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
);
```

## Data Integrity

The seed data is designed to be:
- **Unique**: No duplicate entries
- **Complete**: All required fields populated
- **Consistent**: Parent-child relationships maintained
- **Reference-Safe**: All foreign keys valid

## Future Enhancements

1. **Parametrized Seeding**: Load data from JSON/YAML files
2. **Partial Seeding**: Seed only specific entities
3. **Environment-Based Seeding**: Different data for dev/test/prod
4. **Audit Logging**: Track who seeded and when
5. **Rollback Support**: Ability to remove seed data

---

**Last Updated**: March 2026  
**Author**: GitHub Copilot  
**Version**: 1.0

