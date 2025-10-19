package biz.craftline.server.feature.usermanagement.init;

import biz.craftline.server.config.security.JwtAuthenticationFilter;
import biz.craftline.server.config.security.JwtTokenProvider;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=false",
        "app.jwt.secret=testSecret",
        "app.jwt.expiration=3600000"
})
public class RolePermissionInitializerIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void initializerCreatesPermissionsAndExpandsWildcardForSystemAdmin() {
        // Ensure a concrete permission that should be present
        boolean hasUserCreate = permissionRepository.findByName("user.create").isPresent();
        assertTrue(hasUserCreate, "permission 'user.create' should exist after initialization");

        RoleEntity systemAdmin = roleRepository.findByName("SystemAdmin").orElse(null);
        assertNotNull(systemAdmin, "SystemAdmin role should have been created");

        Set<String> rolePermNames = systemAdmin.getPermissions().stream()
                .map(PermissionEntity::getName)
                .collect(Collectors.toSet());

        // Because SystemAdmin has '*' in the JSON, it should have been expanded to include concrete permissions like user.create
        assertTrue(rolePermNames.contains("user.create"), "SystemAdmin should have mapping to 'user.create' via wildcard expansion");
    }

    @Test
    public void jwtFilterAddsNormalizedAuthorities() throws Exception {
        String token = jwtTokenProvider.generateTokenWithPermissions("testuser", List.of("user.create"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set in SecurityContext");

        Set<String> authNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Expect either original, normalized (USER_CREATE) or alternate (CREATE_USER)
        assertTrue(authNames.contains("user.create") || authNames.contains("USER_CREATE") || authNames.contains("CREATE_USER"),
                "Normalized authorities should include one of the expected forms");
    }
}

