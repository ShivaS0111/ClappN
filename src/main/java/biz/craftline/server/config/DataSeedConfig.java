package biz.craftline.server.config;

import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * Database Seed Configuration
 * Loads initial data for Business Types, Categories, Roles, and Permissions.
 * RBAC names match db/role-permissions-data.sql and @RequirePermission controllers.
 * Demo business/store data is gated by {@code app.seed.demo-data} (false in prod).
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeedConfig {

    @Value("${app.seed.demo-data:true}")
    private boolean seedDemoData;

    @Value("${app.seed.rbac-align:true}")
    private boolean seedRbacAlign;

    @Bean
    public CommandLineRunner seedDatabase(ApplicationContext applicationContext) {
        return args -> {
            try {
                log.info("Starting database seed initialization (rbacAlign={}, demoData={})...",
                        seedRbacAlign, seedDemoData);
                executeSeed(applicationContext);
            } catch (Exception e) {
                log.error("Error initializing seed", e);
            }
        };
    }

    private void executeSeed(ApplicationContext applicationContext) {
        try {
            Object businessTypeRepo = getRepositoryBean(applicationContext, "businessTypeJpaRepository");
            Object categoryRepo = getRepositoryBean(applicationContext, "categoryJpaRepository");
            Object businessRepo = getRepositoryBean(applicationContext, "businessEntityJpaRepository");
            Object storeRepo = getRepositoryBean(applicationContext, "storeRepository");

            PermissionRepository permissionRepo = applicationContext.getBean(PermissionRepository.class);
            RoleRepository roleRepo = applicationContext.getBean(RoleRepository.class);
            TransactionTemplate tx = new TransactionTemplate(
                    applicationContext.getBean(PlatformTransactionManager.class));

            if (seedRbacAlign) {
                tx.executeWithoutResult(status -> alignRbacCatalog(permissionRepo, roleRepo));
            }

            if (seedDemoData && businessTypeRepo != null && isRepositoryEmpty(businessTypeRepo)) {
                log.info("Starting business/category/store seed...");
                seedBusinessTypes(businessTypeRepo);
                seedCategories(categoryRepo);
                seedBusinessAndStores(businessRepo, storeRepo);
                log.info("Database seed completed successfully!");
            } else if (!seedDemoData) {
                log.info("Demo data seed disabled (app.seed.demo-data=false).");
            } else {
                log.info("Business types already populated. Skipping business/category/store seed.");
            }
        } catch (Exception e) {
            log.error("Error executing seed", e);
        }
    }

    private void alignRbacCatalog(PermissionRepository permissionRepo, RoleRepository roleRepo) {
        renameLegacyRoles(roleRepo);
        seedPermissions(permissionRepo);
        seedRoles(roleRepo);
        seedRolePermissions(permissionRepo, roleRepo);
    }

    private void renameLegacyRoles(RoleRepository roleRepo) {
        int renamed = 0;
        for (Map.Entry<String, String> entry : RbacSeedData.LEGACY_ROLE_RENAMES.entrySet()) {
            String legacy = entry.getKey();
            String canonical = entry.getValue();
            Optional<RoleEntity> legacyRole = roleRepo.findByName(legacy);
            if (legacyRole.isEmpty()) {
                continue;
            }
            Optional<RoleEntity> canonicalRole = roleRepo.findByName(canonical);
            if (canonicalRole.isPresent()) {
                // Both exist — do not delete legacy (may still be referenced by user_roles FK)
                log.warn("Skipping delete of legacy role '{}' because canonical '{}' already exists",
                        legacy, canonical);
                continue;
            }
            RoleEntity role = legacyRole.get();
            role.setName(canonical);
            roleRepo.save(role);
            renamed++;
            log.info("Renamed role '{}' → '{}'", legacy, canonical);
        }
        if (renamed > 0) {
            log.info("Renamed {} legacy role name(s) to SCREAMING_SNAKE", renamed);
        }
    }

    private void seedPermissions(PermissionRepository permissionRepo) {
        int created = 0;
        for (String name : RbacSeedData.PERMISSIONS) {
            if (permissionRepo.findByName(name).isEmpty()) {
                permissionRepo.save(new PermissionEntity(name));
                created++;
            }
        }
        log.info("Permissions catalog ready ({} new, {} total defined)", created, RbacSeedData.PERMISSIONS.length);
    }

    private void seedRoles(RoleRepository roleRepo) {
        int created = 0;
        for (String name : RbacSeedData.ROLES) {
            if (roleRepo.findByName(name).isEmpty()) {
                RoleEntity role = new RoleEntity();
                role.setName(name);
                roleRepo.save(role);
                created++;
            }
        }
        log.info("Roles catalog ready ({} new, {} total defined)", created, RbacSeedData.ROLES.length);
    }

    private void seedRolePermissions(PermissionRepository permissionRepo, RoleRepository roleRepo) {
        Map<String, PermissionEntity> permissionsByName = new HashMap<>();
        for (PermissionEntity p : permissionRepo.findAll()) {
            permissionsByName.put(p.getName(), p);
        }

        Map<String, List<String>> roleMap = RbacSeedData.rolePermissionMap();

        // Exact sync: each known role gets precisely the canonical permission set (not additive).
        for (String roleName : RbacSeedData.ROLES) {
            roleRepo.findByName(roleName).ifPresent(role -> {
                Set<PermissionEntity> desired = new HashSet<>();
                if ("SYSTEM_ADMIN".equals(roleName)) {
                    desired.addAll(permissionsByName.values());
                } else if (roleMap.containsKey(roleName)) {
                    for (String permName : roleMap.get(roleName)) {
                        PermissionEntity perm = permissionsByName.get(permName);
                        if (perm != null) {
                            desired.add(perm);
                        } else {
                            log.warn("Permission '{}' missing while assigning to role '{}'", permName, roleName);
                        }
                    }
                }
                // Mutate the persistent collection so join rows are deleted/inserted correctly
                if (role.getPermissions() == null) {
                    role.setPermissions(new HashSet<>());
                }
                role.getPermissions().clear();
                role.getPermissions().addAll(desired);
                roleRepo.save(role);
                log.info("Synced role '{}' to {} permission(s)", roleName, desired.size());
            });
        }
    }

    private Object getRepositoryBean(ApplicationContext context, String beanName) {
        try {
            return context.getBean(beanName);
        } catch (Exception e) {
            log.debug("Repository bean '{}' not found", beanName);
            return null;
        }
    }

    private boolean isRepositoryEmpty(Object repository) {
        try {
            Object countResult = repository.getClass()
                    .getMethod("count")
                    .invoke(repository);
            return ((Number) countResult).longValue() == 0;
        } catch (Exception e) {
            log.warn("Could not check repository count", e);
            return false;
        }
    }

    private void seedBusinessTypes(Object repository) {
        try {
            log.info("Seeding Business Types...");
            List<Map<String, Object>> businessTypes = new ArrayList<>();

            String[][] types = {
                    {"1", "Retail Store", "A place that sells goods directly to consumers."},
                    {"2", "E-Commerce Platform", "Online marketplace for buying and selling products."},
                    {"3", "Supermarket", "Large retail store offering diverse consumer goods."},
                    {"4", "Shopping Mall", "Multi-store shopping center with various retailers."},
                    {"5", "Department Store", "Large store divided into multiple departments."},
                    {"6", "Restaurant", "An establishment where meals are prepared and served to customers."},
                    {"7", "Cafe & Coffee Shop", "Serves beverages and light food items."},
                    {"8", "Fast Food Chain", "Quick service restaurant with standardized menu."},
                    {"9", "Bakery", "Produces and sells baked goods and pastries."},
                    {"10", "Pizzeria", "Specializes in pizza preparation and delivery."},
                    {"11", "Ice Cream Parlor", "Serves ice cream and frozen desserts."},
                    {"12", "Bar & Pub", "Serves alcoholic beverages and food."},
                    {"13", "Food Truck", "Mobile food service operating from vehicle."},
                    {"14", "Catering Service", "Provides food service for events and functions."},
                    {"15", "Bakery & Confectionery", "Bakes and sells cakes, bread, and sweets."},
                    {"16", "IT Services", "A business offering technology solutions, support, and consulting."},
                    {"17", "Software Development", "Develops custom software and applications."},
                    {"18", "Web Design Agency", "Creates and maintains websites for businesses."},
                    {"19", "Digital Marketing Agency", "Provides online marketing and advertising services."},
                    {"20", "Data Analytics Firm", "Analyzes data to provide business insights."},
                    {"21", "Cybersecurity Services", "Protects businesses from digital threats."},
                    {"22", "Cloud Services Provider", "Offers cloud computing and storage solutions."},
                    {"23", "Construction", "A business involved in building infrastructure, homes, and commercial properties."},
                    {"24", "Real Estate", "Deals with buying, selling, and managing properties."},
                    {"25", "Property Management", "Manages rental properties and tenant relations."},
                    {"26", "Interior Design", "Designs interior spaces and provides furniture solutions."},
                    {"27", "Architecture Firm", "Designs buildings and structures."},
                    {"28", "Home Renovation", "Renovates and remodels residential properties."},
                    {"29", "Commercial Real Estate", "Focuses on commercial property transactions."},
                    {"30", "Real Estate Brokerage", "Acts as intermediary for property sales and purchases."},
                    {"31", "Healthcare Clinic", "Provides medical services to patients on an outpatient basis."},
                    {"32", "Hospital", "Provides comprehensive medical and surgical services."},
                    {"33", "Dental Clinic", "Provides dental care and orthodontic services."},
                    {"34", "Opticals Store", "Sells eyeglasses, contact lenses, and eye care products."},
                    {"35", "Pharmacy", "Dispenses medicines and health products."},
                    {"36", "Fitness Center", "Provides gymnasium and fitness training facilities."},
                    {"37", "Yoga Studio", "Offers yoga classes and wellness programs."},
                    {"38", "Spa & Salon", "Provides beauty, wellness, and relaxation services."},
                    {"39", "Therapy Center", "Offers mental health and therapeutic services."},
                    {"40", "Wellness Retreat", "Provides holistic health and wellness programs."},
                    {"41", "Education & Training", "Offers educational courses and professional training."},
                    {"42", "School", "Provides primary and secondary education."},
                    {"43", "College/University", "Offers higher education and degree programs."},
                    {"44", "Coaching Center", "Provides test preparation and academic tutoring."},
                    {"45", "Online Course Platform", "Offers digital courses and e-learning."},
                    {"46", "Skill Development Center", "Trains in professional and technical skills."},
                    {"47", "Language Institute", "Teaches foreign languages and communication."},
                    {"48", "Music Academy", "Provides music lessons and training."},
                    {"49", "Logistics & Transportation", "Handles the movement of goods and people from one place to another."},
                    {"50", "Taxi Service", "Provides passenger transportation services."},
                    {"51", "Courier Service", "Delivers packages and documents."},
                    {"52", "Shipping Company", "Transports cargo via sea and air."},
                    {"53", "Warehouse Management", "Manages storage and distribution facilities."},
                    {"54", "Car Rental", "Rents vehicles to customers."},
                    {"55", "Moving & Relocation", "Assists in household and office relocations."},
                    {"56", "Public Transportation", "Operates buses, trains, or metro services."},
                    {"57", "Financial Services", "Provides banking, investment, insurance, and other money-related services."},
                    {"58", "Bank", "Provides banking and financial products."},
                    {"59", "Insurance Company", "Offers insurance products and coverage."},
                    {"60", "Investment Firm", "Manages investments and portfolios."},
                    {"61", "Financial Advisory", "Provides financial planning and advice."},
                    {"62", "Stock Brokerage", "Facilitates buying and selling of stocks."},
                    {"63", "Microfinance", "Provides small loans to entrepreneurs."},
                    {"64", "Accounting Firm", "Provides bookkeeping and tax services."},
                    {"65", "Loan Agency", "Provides personal and business loans."},
                    {"66", "Entertainment", "Produces and distributes content for leisure, including movies, music, and events."},
                    {"67", "Movie Theater", "Shows films to audiences."},
                    {"68", "Music Production", "Records and produces music albums."},
                    {"69", "Event Management", "Organizes and manages events and conferences."},
                    {"70", "Gaming Studio", "Develops video games."},
                    {"71", "Broadcasting Company", "Produces and broadcasts TV and radio content."},
                    {"72", "Amusement Park", "Operates rides and entertainment attractions."},
                    {"73", "Concert Venue", "Hosts live music performances."},
                    {"74", "Theater", "Produces and stages theatrical performances."},
                    {"75", "Streaming Service", "Provides on-demand video and audio content."},
                    {"76", "Bike Mechanic Services", "Repairs and maintains motorcycles and bicycles."},
                    {"77", "Car Mechanic Services", "Repairs and maintains automobiles."},
                    {"78", "Plumbing Services", "Provides plumbing repair and installation."},
                    {"79", "Electrical Services", "Provides electrical repair and installation."},
                    {"80", "HVAC Services", "Provides heating, ventilation, and air conditioning services."},
                    {"81", "Cleaning Services", "Provides residential and commercial cleaning."},
                    {"82", "Laundry Services", "Provides laundry and dry cleaning services."},
                    {"83", "Photography Studio", "Provides professional photography services."},
                    {"84", "Video Production", "Produces videos and documentaries."},
                    {"85", "Pet Grooming", "Provides pet grooming and care services."}
            };

            for (String[] type : types) {
                Map<String, Object> typeMap = new HashMap<>();
                typeMap.put("id", Long.parseLong(type[0]));
                typeMap.put("businessName", type[1]);
                typeMap.put("description", type[2]);
                typeMap.put("status", 1);
                businessTypes.add(typeMap);
            }

            invokeSaveAll(repository, businessTypes);
            log.info("Successfully seeded {} business types", businessTypes.size());
        } catch (Exception e) {
            log.error("Error seeding business types", e);
        }
    }

    private void seedCategories(Object repository) {
        try {
            log.info("Seeding Categories...");
            List<Map<String, Object>> categories = new ArrayList<>();
            
            String[] topCategories = {
                    "Electronics", "Fashion", "Home & Kitchen", "Sports & Outdoors", "Books & Stationery",
                    "Automotive", "Toys & Games", "Beauty & Personal Care", "Health & Wellness", "Grocery & Gourmet",
                    "Jewelry", "Baby Products", "Pet Supplies", "Garden & Tools", "Office Supplies",
                    "Music Instruments", "Movies & Entertainment", "Industrial Supplies", "Travel & Luggage", "Software"
            };
            
            for (int i = 0; i < topCategories.length; i++) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", (long) (i + 1));
                cat.put("name", topCategories[i]);
                cat.put("status", 1);
                categories.add(cat);
            }
            
            invokeSaveAll(repository, categories);
            log.info("Successfully seeded {} categories", categories.size());
        } catch (Exception e) {
            log.error("Error seeding categories", e);
        }
    }

    private void seedBusinessAndStores(Object businessRepo, Object storeRepo) {
        try {
            log.info("Seeding Business and Store entities...");
            
            if (businessRepo != null && isRepositoryEmpty(businessRepo)) {
                List<Map<String, Object>> businesses = new ArrayList<>();
                
                Map<String, Object> business = new HashMap<>();
                business.put("businessName", "Sri Laxmi Venkateshwara Companies Ltd");
                business.put("description", "Premium retail and optical services provider");
                business.put("status", 1);
                business.put("contact", "+91-9876543210");
                business.put("email", "contact@srilaxmi.com");
                business.put("website", "www.srilaxmi.com");
                business.put("address", "123 Main Street, City Center");
                business.put("latitude", 28.6139);
                business.put("longitude", 77.2090);
                business.put("bannerUrl", "https://example.com/banner/business-banner-1.jpg");
                business.put("galleryUrls", createGalleryJsonArray(Arrays.asList(
                        "https://example.com/gallery/business-1.jpg",
                        "https://example.com/gallery/business-2.jpg",
                        "https://example.com/gallery/business-3.jpg",
                        "https://example.com/gallery/business-4.jpg"
                )));
                
                businesses.add(business);
                invokeSaveAll(businessRepo, businesses);
                log.info("Successfully seeded 1 business entity");
                
                if (storeRepo != null && isRepositoryEmpty(storeRepo)) {
                    List<Map<String, Object>> stores = new ArrayList<>();
                    
                    Map<String, Object> store = new HashMap<>();
                    store.put("storeName", "SVS Opticals");
                    store.put("description", "Professional optical services and eyewear");
                    store.put("status", 1);
                    store.put("businessType", 34L);
                    store.put("address", "456 Sub Street, Downtown");
                    store.put("email", "svs.opticals@srilaxmi.com");
                    store.put("phone", "+91-9876543211");
                    store.put("manager", "Mr. Sharma");
                    store.put("hours", "9:00 AM - 9:00 PM");
                    store.put("bannerUrl", "https://example.com/banner/store-banner-1.jpg");
                    store.put("galleryUrls", createGalleryJsonArray(Arrays.asList(
                            "https://example.com/gallery/store-1.jpg",
                            "https://example.com/gallery/store-2.jpg",
                            "https://example.com/gallery/store-3.jpg",
                            "https://example.com/gallery/store-4.jpg",
                            "https://example.com/gallery/store-5.jpg"
                    )));
                    
                    stores.add(store);
                    invokeSaveAll(storeRepo, stores);
                    log.info("Successfully seeded 1 store entity");
                }
            }
        } catch (Exception e) {
            log.error("Error seeding business and stores", e);
        }
    }

    private void invokeSaveAll(Object repository, List<?> data) throws Exception {
        if (repository == null || data == null || data.isEmpty()) {
            return;
        }
        repository.getClass()
                .getMethod("saveAll", java.lang.Iterable.class)
                .invoke(repository, data);
    }

    private String createGalleryJsonArray(List<String> urls) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(urls);
        } catch (Exception e) {
            log.warn("Failed to create gallery JSON", e);
            return "[]";
        }
    }
}

