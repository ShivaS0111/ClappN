package biz.craftline.server.thorough;

import biz.craftline.server.config.RbacSeedData;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedPackageEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreOfferedPackageRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BrandEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BrandJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.CategoryJpaRepository;
import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import biz.craftline.server.feature.customermanagement.infra.repository.CustomerRepository;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Idempotent thorough test data: one user per RBAC role, shared business/stores,
 * catalog templates, store offerings, memberships, and a sample customer.
 * Also seeds an isolation dataset (Business B + scoped special users) for tenant QA.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThoroughFeatureSeedService {

    public static final String PASSWORD = "Test@12345";
    public static final String EMAIL_PREFIX = "thorough.";
    public static final String EMAIL_DOMAIN = "@clapp.test";
    public static final String BUSINESS_NAME = "Thorough Test Business";
    /** Alias — Business A is the existing thorough business. */
    public static final String BUSINESS_A_NAME = BUSINESS_NAME;
    public static final String BUSINESS_B_NAME = "Thorough Isolation Business B";
    public static final String STORE_A = "Thorough Store Alpha";
    public static final String STORE_B = "Thorough Store Beta";
    public static final String STORE_A3 = "Thorough Store Gamma";
    public static final String STORE_B1 = "Thorough Store B1";
    public static final String STORE_B2 = "Thorough Store B2";
    public static final String BRAND_NAME = "Thorough Brand";
    public static final String PRODUCT_NAME = "Thorough Template Product";
    public static final String PRODUCT_B_ALIAS = "Thorough Isolation Product B";
    public static final String SERVICE_NAME = "Thorough Template Service";
    public static final String CATEGORY_NAME = "Thorough Category";

    /** Special isolation users (keys for {@link SeedSnapshot#getSpecialUserIds()}). */
    public static final String SPECIAL_BUSINESS_A = "scope.business_a";
    public static final String SPECIAL_STORE_A1 = "scope.store_a1";
    public static final String SPECIAL_STORE_A1_A2 = "scope.store_a1_a2";
    public static final String SPECIAL_BUSINESS_B = "scope.business_b";
    public static final String SPECIAL_NONE = "scope.none";
    public static final String SPECIAL_INACTIVE = "scope.inactive";
    /** Disabled account for auth suite — email {@link #DISABLED_EMAIL}. */
    public static final String SPECIAL_DISABLED = "auth.disabled";
    public static final String DISABLED_EMAIL = "thorough.auth.disabled@clapp.test";

    private static final Set<String> BUSINESS_LEVEL = Set.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER"
    );
    private static final Set<String> PLATFORM_ONLY = Set.of("SYSTEM_ADMIN");
    private static final Set<String> NO_TENANCY = Set.of("CUSTOMER", "GUEST");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MembershipRepository membershipRepository;
    private final BusinessEntityJpaRepository businessRepository;
    private final StoreRepository storeRepository;
    private final BusinessTypeJpaRepository businessTypeRepository;
    private final BrandJpaRepository brandRepository;
    private final CategoryJpaRepository categoryRepository;
    private final BusinessProductJpaRepository productRepository;
    private final BusinessServicesJpaRepository serviceRepository;
    private final ProductsOfferedByStoreRepository storeProductRepository;
    private final ServicesOfferedByStoreRepository storeServiceRepository;
    private final StoreOfferedPackageRepository packageRepository;
    private final CustomerRepository customerRepository;

    @Getter
    @Builder
    public static class SeedSnapshot {
        /** Business A (existing thorough business). */
        private Long businessId;
        /** Store A1 (Thorough Store Alpha). */
        private Long storeAId;
        /** Store A2 (Thorough Store Beta). */
        private Long storeBId;
        private Long businessTypeId;
        private Long brandId;
        private Long categoryId;
        private Long productTemplateId;
        private Long serviceTemplateId;
        private Long storeProductId;
        private Long storeServiceId;
        private Long packageId;
        private Long customerId;
        private Map<String, Long> userIdsByRole;
        private Map<String, String> emailsByRole;
        private int membershipCount;
        private int roleCount;
        private int permissionMappedRoles;
        /* --- Isolation dataset (Business B + special scoped users) --- */
        private Long businessBId;
        private Long storeA3Id;
        private Long storeB1Id;
        private Long storeB2Id;
        private Long customerBId;
        private Long storeProductBId;
        /** Special user ids keyed by scope.* constants. */
        private Map<String, Long> specialUserIds;
        /** Special user emails keyed by scope.* constants. */
        private Map<String, String> specialEmails;
    }

    @Transactional
    public SeedSnapshot seed() {
        log.info("Thorough seed starting…");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        BusinessTypeEntity businessType = businessTypeRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    BusinessTypeEntity t = new BusinessTypeEntity();
                    t.setBusinessName("Thorough Retail Type");
                    t.setDescription("Seeded for thorough tests");
                    t.setStatus(1);
                    return businessTypeRepository.save(t);
                });

        BrandEntity brand = brandRepository.findAll().stream()
                .filter(b -> BRAND_NAME.equalsIgnoreCase(b.getName()))
                .findFirst()
                .orElseGet(() -> {
                    BrandEntity b = new BrandEntity();
                    b.setName(BRAND_NAME);
                    b.setDescription("Thorough brand");
                    return brandRepository.save(b);
                });

        CategoryEntity category = categoryRepository.findAll().stream()
                .filter(c -> CATEGORY_NAME.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseGet(() -> {
                    CategoryEntity c = new CategoryEntity();
                    c.setName(CATEGORY_NAME);
                    c.setDescription("Thorough category");
                    return categoryRepository.save(c);
                });

        BusinessProductEntity product = productRepository.findAll().stream()
                .filter(p -> PRODUCT_NAME.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElseGet(() -> {
                    BusinessProductEntity p = new BusinessProductEntity();
                    p.setName(PRODUCT_NAME);
                    p.setDescription("Thorough product template");
                    p.setStatus(1);
                    p.setBusinessType(businessType);
                    p.setBrand(brand);
                    p.setCategories(List.of(category));
                    p.setAmount(499.0f);
                    p.setCurrency(1L);
                    return productRepository.save(p);
                });

        BusinessServiceEntity service = serviceRepository.findAll().stream()
                .filter(s -> SERVICE_NAME.equalsIgnoreCase(s.getServiceName()))
                .findFirst()
                .orElseGet(() -> {
                    BusinessServiceEntity s = new BusinessServiceEntity();
                    s.setServiceName(SERVICE_NAME);
                    s.setDescription("Thorough service template");
                    s.setStatus(1);
                    s.setBusinessType(businessType);
                    s.setCategories(List.of(category));
                    s.setAmount(299.0f);
                    s.setCurrency(1L);
                    s.setDuration(60L);
                    return serviceRepository.save(s);
                });

        BusinessEntity business = businessRepository.findByNameContaining("Thorough Test").stream()
                .filter(b -> BUSINESS_NAME.equalsIgnoreCase(b.getBusinessName()))
                .findFirst()
                .orElseGet(() -> businessRepository.save(BusinessEntity.builder()
                        .businessName(BUSINESS_NAME)
                        .description("Seeded thorough test business")
                        .status(1)
                        .email("thorough.biz@clapp.test")
                        .contact("+10000000000")
                        .stores(new HashSet<>())
                        .build()));

        // Avoid Hibernate session identity clashes with business.stores
        Long businessId = business.getId();
        if (business.getStores() != null) {
            business.getStores().clear();
        }

        StoreEntity storeA = ensureStore(businessId, STORE_A, businessType.getId());
        StoreEntity storeB = ensureStore(businessId, STORE_B, businessType.getId());

        StoreOfferedProductEntity storeProduct = storeProductRepository.findAll().stream()
                .filter(p -> Objects.equals(p.getStoreId(), storeA.getId())
                        && PRODUCT_NAME.equalsIgnoreCase(p.getAliasName()))
                .findFirst()
                .orElseGet(() -> storeProductRepository.save(StoreOfferedProductEntity.builder()
                        .aliasName(PRODUCT_NAME)
                        .description("Store offering of template product")
                        .storeId(storeA.getId())
                        .businessId(businessId)
                        .businessProductId(product.getId())
                        .status(1)
                        .build()));

        StoreOfferedServiceEntity storeService = storeServiceRepository.findAll().stream()
                .filter(s -> Objects.equals(s.getStoreId(), storeA.getId())
                        && SERVICE_NAME.equalsIgnoreCase(s.getAliasName()))
                .findFirst()
                .orElseGet(() -> storeServiceRepository.save(StoreOfferedServiceEntity.builder()
                        .aliasName(SERVICE_NAME)
                        .description("Store offering of template service")
                        .storeId(storeA.getId())
                        .businessId(businessId)
                        .businessServiceId(service.getId())
                        .status(1)
                        .build()));

        StoreOfferedPackageEntity pkg = packageRepository.findAll().stream()
                .filter(p -> Objects.equals(p.getStoreId(), storeA.getId())
                        && "Thorough Starter Pack".equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Set<Long> products = new HashSet<>();
                    products.add(storeProduct.getId());
                    Set<Long> services = new HashSet<>();
                    services.add(storeService.getId());
                    return packageRepository.save(StoreOfferedPackageEntity.builder()
                            .name("Thorough Starter Pack")
                            .description("Seeded package")
                            .storeId(storeA.getId())
                            .status(1)
                            .price(699.0)
                            .available(true)
                            .productIds(products)
                            .serviceIds(services)
                            .build());
                });

        CustomerEntity customer = customerRepository.findAll().stream()
                .filter(c -> "thorough.customer@clapp.test".equalsIgnoreCase(c.getEmail()))
                .findFirst()
                .orElseGet(() -> {
                    CustomerEntity c = new CustomerEntity();
                    c.setFirstName("Thorough");
                    c.setLastName("Customer");
                    c.setEmail("thorough.customer@clapp.test");
                    c.setPhone("+19999999999");
                    c.setStoreId(storeA.getId());
                    c.setBusinessId(businessId);
                    return customerRepository.save(c);
                });
        // Keep tenant pointers current if stores were recreated
        customer.setStoreId(storeA.getId());
        customer.setBusinessId(businessId);
        customer = customerRepository.save(customer);

        Map<String, Long> userIds = new LinkedHashMap<>();
        Map<String, String> emails = new LinkedHashMap<>();
        int memberships = 0;

        for (String roleName : RbacSeedData.ROLES) {
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role missing (run RBAC seed): " + roleName));

            String email = emailForRole(roleName);
            emails.put(roleName, email);

            // Load with roles so clear()/replace actually updates user_roles join rows
            UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                    .or(() -> userRepository.findByEmail(email))
                    .orElseGet(() -> {
                UserEntity u = new UserEntity();
                u.setFullName("Thorough " + roleName.replace('_', ' '));
                u.setEmail(email);
                u.setPassword(encoder.encode(PASSWORD));
                u.setEnabled(true);
                u.setVerified(1);
                u.setAccountNonLocked(true);
                u.setAccountNonExpired(true);
                u.setCredentialsNonExpired(true);
                u.setRoles(new HashSet<>());
                return userRepository.save(u);
            });
            // keep password predictable for probes
            user.setPassword(encoder.encode(PASSWORD));
            user.setEnabled(true);
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }

            if (PLATFORM_ONLY.contains(roleName) || NO_TENANCY.contains(roleName)) {
                user.getRoles().clear();
                user.getRoles().add(role);
                userRepository.save(user);
                userIds.put(roleName, user.getId());
                continue;
            }

            // Tenant roles live on membership only — strip any leftover platform roles
            // (e.g. accidental SYSTEM_ADMIN on user_roles would make every probe unrestricted)
            user.getRoles().clear();
            userRepository.saveAndFlush(user);
            userIds.put(roleName, user.getId());

            MembershipEntity membership = membershipRepository
                    .findByUserIdAndBusinessId(user.getId(), businessId)
                    .orElseGet(() -> MembershipEntity.builder()
                            .userId(user.getId())
                            .businessId(businessId)
                            .status(MembershipEntity.STATUS_ACTIVE)
                            .roles(new HashSet<>())
                            .storeScopes(new HashSet<>())
                            .build());

            membership.setStatus(MembershipEntity.STATUS_ACTIVE);
            if (membership.getRoles() == null) membership.setRoles(new HashSet<>());
            membership.getRoles().clear();
            membership.getRoles().add(role);

            if (membership.getStoreScopes() == null) membership.setStoreScopes(new HashSet<>());
            membership.getStoreScopes().clear();
            if (!BUSINESS_LEVEL.contains(roleName)) {
                membership.getStoreScopes().add(storeA.getId());
                // give STORE_OWNER both stores to exercise multi-scope
                if ("STORE_OWNER".equals(roleName) || "STORE_MANAGER".equals(roleName)) {
                    membership.getStoreScopes().add(storeB.getId());
                }
            }

            if (membership.getProfile() == null) {
                membership.setProfile(EmployeeProfileEntity.builder()
                        .userId(user.getId())
                        .membership(membership)
                        .employeeNumber("THR-" + roleName)
                        .hireDate("2026-01-01")
                        .jobTitle(roleName)
                        .name(user.getFullName())
                        .email(email)
                        .build());
            } else {
                EmployeeProfileEntity profile = membership.getProfile();
                profile.setEmployeeNumber("THR-" + roleName);
                profile.setJobTitle(roleName);
                profile.setName(user.getFullName());
                profile.setEmail(email);
            }

            membershipRepository.save(membership);
            memberships++;
        }

        Map<String, List<String>> mapped = RbacSeedData.rolePermissionMap();

        IsolationDataset isolation = ensureIsolationDataset(
                encoder, businessId, storeA, storeB, businessType.getId(), product.getId());

        SeedSnapshot snapshot = SeedSnapshot.builder()
                .businessId(businessId)
                .storeAId(storeA.getId())
                .storeBId(storeB.getId())
                .businessTypeId(businessType.getId())
                .brandId(brand.getId())
                .categoryId(category.getId())
                .productTemplateId(product.getId())
                .serviceTemplateId(service.getId())
                .storeProductId(storeProduct.getId())
                .storeServiceId(storeService.getId())
                .packageId(pkg.getId())
                .customerId(customer.getId())
                .userIdsByRole(userIds)
                .emailsByRole(emails)
                .membershipCount(memberships + isolation.extraMemberships())
                .roleCount(RbacSeedData.ROLES.length)
                .permissionMappedRoles(mapped.size())
                .businessBId(isolation.businessBId())
                .storeA3Id(isolation.storeA3Id())
                .storeB1Id(isolation.storeB1Id())
                .storeB2Id(isolation.storeB2Id())
                .customerBId(isolation.customerBId())
                .storeProductBId(isolation.storeProductBId())
                .specialUserIds(isolation.specialUserIds())
                .specialEmails(isolation.specialEmails())
                .build();

        log.info("Thorough seed complete: businessA={}, businessB={}, stores={}/{}/{} + B{}/{}, users={}, special={}, memberships={}",
                businessId, isolation.businessBId(),
                storeA.getId(), storeB.getId(), isolation.storeA3Id(),
                isolation.storeB1Id(), isolation.storeB2Id(),
                userIds.size(), isolation.specialUserIds().size(),
                memberships + isolation.extraMemberships());
        return snapshot;
    }

    /**
     * Deterministic Business A/B isolation dataset + special scoped users.
     * Does not alter existing per-role users used by FullApiRoleAuthzIT.
     */
    private IsolationDataset ensureIsolationDataset(
            BCryptPasswordEncoder encoder,
            Long businessAId,
            StoreEntity storeA1,
            StoreEntity storeA2,
            Long businessTypeId,
            Long productTemplateId) {

        StoreEntity storeA3 = ensureStore(businessAId, STORE_A3, businessTypeId);

        BusinessEntity businessB = businessRepository.findByNameContaining("Thorough Isolation").stream()
                .filter(b -> BUSINESS_B_NAME.equalsIgnoreCase(b.getBusinessName()))
                .findFirst()
                .orElseGet(() -> businessRepository.save(BusinessEntity.builder()
                        .businessName(BUSINESS_B_NAME)
                        .description("Seeded thorough isolation business B")
                        .status(1)
                        .email("thorough.biz.b@clapp.test")
                        .contact("+10000000002")
                        .stores(new HashSet<>())
                        .build()));
        Long businessBId = businessB.getId();
        if (businessB.getStores() != null) {
            businessB.getStores().clear();
        }

        StoreEntity storeB1 = ensureStore(businessBId, STORE_B1, businessTypeId);
        StoreEntity storeB2 = ensureStore(businessBId, STORE_B2, businessTypeId);

        StoreOfferedProductEntity storeProductB = storeProductRepository.findAll().stream()
                .filter(p -> Objects.equals(p.getStoreId(), storeB1.getId())
                        && PRODUCT_B_ALIAS.equalsIgnoreCase(p.getAliasName()))
                .findFirst()
                .orElseGet(() -> storeProductRepository.save(StoreOfferedProductEntity.builder()
                        .aliasName(PRODUCT_B_ALIAS)
                        .description("Isolation store product on Business B")
                        .storeId(storeB1.getId())
                        .businessId(businessBId)
                        .businessProductId(productTemplateId)
                        .status(1)
                        .build()));

        CustomerEntity customerB = customerRepository.findAll().stream()
                .filter(c -> "thorough.customer.b@clapp.test".equalsIgnoreCase(c.getEmail()))
                .findFirst()
                .orElseGet(() -> {
                    CustomerEntity c = new CustomerEntity();
                    c.setFirstName("Thorough");
                    c.setLastName("CustomerB");
                    c.setEmail("thorough.customer.b@clapp.test");
                    c.setPhone("+19999999998");
                    c.setStoreId(storeB1.getId());
                    c.setBusinessId(businessBId);
                    return customerRepository.save(c);
                });
        customerB.setStoreId(storeB1.getId());
        customerB.setBusinessId(businessBId);
        customerB = customerRepository.save(customerB);

        Map<String, Long> specialIds = new LinkedHashMap<>();
        Map<String, String> specialEmails = new LinkedHashMap<>();
        int extraMemberships = 0;

        // BUSINESS_OWNER on Biz A — empty store scopes = all A stores
        extraMemberships += upsertSpecialMembership(
                encoder, SPECIAL_BUSINESS_A, "BUSINESS_OWNER", businessAId,
                Set.of(), MembershipEntity.STATUS_ACTIVE, specialIds, specialEmails);

        // STORE_MANAGER only Store A1
        extraMemberships += upsertSpecialMembership(
                encoder, SPECIAL_STORE_A1, "STORE_MANAGER", businessAId,
                Set.of(storeA1.getId()), MembershipEntity.STATUS_ACTIVE, specialIds, specialEmails);

        // STORE_MANAGER scopes A1+A2
        extraMemberships += upsertSpecialMembership(
                encoder, SPECIAL_STORE_A1_A2, "STORE_MANAGER", businessAId,
                Set.of(storeA1.getId(), storeA2.getId()), MembershipEntity.STATUS_ACTIVE,
                specialIds, specialEmails);

        // BUSINESS_OWNER on Biz B
        extraMemberships += upsertSpecialMembership(
                encoder, SPECIAL_BUSINESS_B, "BUSINESS_OWNER", businessBId,
                Set.of(), MembershipEntity.STATUS_ACTIVE, specialIds, specialEmails);

        // User with no membership
        String noneEmail = emailForSpecial(SPECIAL_NONE);
        specialEmails.put(SPECIAL_NONE, noneEmail);
        UserEntity noneUser = ensureSpecialUser(encoder, SPECIAL_NONE, noneEmail);
        noneUser.getRoles().clear();
        userRepository.saveAndFlush(noneUser);
        specialIds.put(SPECIAL_NONE, noneUser.getId());
        // Ensure no leftover memberships
        for (MembershipEntity m : membershipRepository.findByUserId(noneUser.getId())) {
            membershipRepository.delete(m);
        }

        // Membership INACTIVE on Biz A (BUSINESS_OWNER role but inactive)
        extraMemberships += upsertSpecialMembership(
                encoder, SPECIAL_INACTIVE, "BUSINESS_OWNER", businessAId,
                Set.of(), MembershipEntity.STATUS_INACTIVE, specialIds, specialEmails);

        // Disabled user for AuthFullIT (enabled=false) — no membership needed
        specialEmails.put(SPECIAL_DISABLED, DISABLED_EMAIL);
        UserEntity disabledUser = ensureDisabledUser(encoder);
        specialIds.put(SPECIAL_DISABLED, disabledUser.getId());

        return new IsolationDataset(
                businessBId,
                storeA3.getId(),
                storeB1.getId(),
                storeB2.getId(),
                customerB.getId(),
                storeProductB.getId(),
                specialIds,
                specialEmails,
                extraMemberships
        );
    }

    private int upsertSpecialMembership(
            BCryptPasswordEncoder encoder,
            String specialKey,
            String roleName,
            Long businessId,
            Set<Long> storeScopes,
            String status,
            Map<String, Long> specialIds,
            Map<String, String> specialEmails) {

        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role missing (run RBAC seed): " + roleName));

        String email = emailForSpecial(specialKey);
        specialEmails.put(specialKey, email);
        UserEntity user = ensureSpecialUser(encoder, specialKey, email);
        user.getRoles().clear();
        userRepository.saveAndFlush(user);
        specialIds.put(specialKey, user.getId());

        MembershipEntity membership = membershipRepository
                .findByUserIdAndBusinessId(user.getId(), businessId)
                .orElseGet(() -> MembershipEntity.builder()
                        .userId(user.getId())
                        .businessId(businessId)
                        .status(status)
                        .roles(new HashSet<>())
                        .storeScopes(new HashSet<>())
                        .build());

        membership.setStatus(status);
        if (membership.getRoles() == null) membership.setRoles(new HashSet<>());
        membership.getRoles().clear();
        membership.getRoles().add(role);

        if (membership.getStoreScopes() == null) membership.setStoreScopes(new HashSet<>());
        membership.getStoreScopes().clear();
        membership.getStoreScopes().addAll(storeScopes);

        if (membership.getProfile() == null) {
            membership.setProfile(EmployeeProfileEntity.builder()
                    .userId(user.getId())
                    .membership(membership)
                    .employeeNumber("THR-" + specialKey.replace('.', '-'))
                    .hireDate("2026-01-01")
                    .jobTitle(specialKey)
                    .name(user.getFullName())
                    .email(email)
                    .build());
        } else {
            EmployeeProfileEntity profile = membership.getProfile();
            profile.setEmployeeNumber("THR-" + specialKey.replace('.', '-'));
            profile.setJobTitle(specialKey);
            profile.setName(user.getFullName());
            profile.setEmail(email);
        }

        membershipRepository.save(membership);
        return 1;
    }

    private UserEntity ensureSpecialUser(BCryptPasswordEncoder encoder, String specialKey, String email) {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setFullName("Thorough " + specialKey.replace('.', ' '));
                    u.setEmail(email);
                    u.setPassword(encoder.encode(PASSWORD));
                    u.setEnabled(true);
                    u.setVerified(1);
                    u.setAccountNonLocked(true);
                    u.setAccountNonExpired(true);
                    u.setCredentialsNonExpired(true);
                    u.setRoles(new HashSet<>());
                    return userRepository.save(u);
                });
        user.setPassword(encoder.encode(PASSWORD));
        user.setEnabled(true);
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        return user;
    }

    /** Ensures {@link #DISABLED_EMAIL} exists with enabled=false (login must fail). */
    private UserEntity ensureDisabledUser(BCryptPasswordEncoder encoder) {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(DISABLED_EMAIL)
                .or(() -> userRepository.findByEmail(DISABLED_EMAIL))
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setFullName("Thorough Auth Disabled");
                    u.setEmail(DISABLED_EMAIL);
                    u.setPassword(encoder.encode(PASSWORD));
                    u.setEnabled(false);
                    u.setVerified(1);
                    u.setAccountNonLocked(true);
                    u.setAccountNonExpired(true);
                    u.setCredentialsNonExpired(true);
                    u.setRoles(new HashSet<>());
                    return userRepository.save(u);
                });
        user.setPassword(encoder.encode(PASSWORD));
        user.setEnabled(false);
        user.setVerified(1);
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().clear();
        return userRepository.save(user);
    }

    private record IsolationDataset(
            Long businessBId,
            Long storeA3Id,
            Long storeB1Id,
            Long storeB2Id,
            Long customerBId,
            Long storeProductBId,
            Map<String, Long> specialUserIds,
            Map<String, String> specialEmails,
            int extraMemberships
    ) {}

    private StoreEntity ensureStore(Long businessId, String name, Long businessTypeId) {
        return storeRepository.findAll().stream()
                .filter(s -> name.equalsIgnoreCase(s.getStoreName())
                        && s.getBusiness() != null
                        && Objects.equals(s.getBusiness().getId(), businessId))
                .findFirst()
                .orElseGet(() -> {
                    StoreEntity s = StoreEntity.builder()
                            .storeName(name)
                            .description("Thorough seeded store")
                            .status(1)
                            .business(businessRepository.getReferenceById(businessId))
                            .businessType(businessTypeId)
                            .email(name.toLowerCase().replace(' ', '.') + "@clapp.test")
                            .phone("+10000000001")
                            .services(new HashSet<>())
                            .products(new HashSet<>())
                            .build();
                    return storeRepository.save(s);
                });
    }

    public static String emailForRole(String roleName) {
        return EMAIL_PREFIX + roleName.toLowerCase() + EMAIL_DOMAIN;
    }

    /**
     * Email for isolation special users, e.g. {@code thorough.scope.business_a@clapp.test}.
     * Accepts keys with or without the {@code scope.} prefix.
     */
    public static String emailForSpecial(String key) {
        String normalized = key == null ? "" : key.trim().toLowerCase();
        if (!normalized.startsWith("scope.")) {
            normalized = "scope." + normalized;
        }
        return EMAIL_PREFIX + normalized + EMAIL_DOMAIN;
    }
}
