package biz.craftline.server.feature.usermanagement.infra.init;

import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper;

    private static final String RESOURCE_PATH = "roles-permissions.json";

    @Transactional
    public void initializeFromResource() throws Exception {
        Resource resource = new ClassPathResource(RESOURCE_PATH);
        if (!resource.exists()) {
            log.warn("Role/permission resource '{}' not found on classpath, skipping initialization.", RESOURCE_PATH);
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);

            Set<String> allPermissions = new HashSet<>();

            // 1) Collect permissions from permissionCategories
            JsonNode permissionCategories = root.path("permissionCategories");
            if (permissionCategories.isObject()) {
                Iterator<java.util.Map.Entry<String, JsonNode>> catIter = permissionCategories.fields();
                while (catIter.hasNext()) {
                    java.util.Map.Entry<String, JsonNode> e = catIter.next();
                    JsonNode arr = e.getValue();
                    if (arr.isArray()) {
                        for (JsonNode p : arr) allPermissions.add(p.asText());
                    }
                }
            }

            // 2) Collect permissions defined per role
            JsonNode rolePermissions = root.path("rolePermissions");
            if (rolePermissions.isObject()) {
                Iterator<java.util.Map.Entry<String, JsonNode>> roles = rolePermissions.fields();
                while (roles.hasNext()) {
                    java.util.Map.Entry<String, JsonNode> r = roles.next();
                    JsonNode perms = r.getValue().path("permissions");
                    if (perms.isArray()) {
                        for (JsonNode p : perms) allPermissions.add(p.asText());
                    }
                }
            }

            // 3) Collect permissionHierarchy entries (add raw patterns)
            JsonNode permissionHierarchy = root.path("permissionHierarchy");
            if (permissionHierarchy.isObject()) {
                Iterator<java.util.Map.Entry<String, JsonNode>> hier = permissionHierarchy.fields();
                while (hier.hasNext()) {
                    java.util.Map.Entry<String, JsonNode> e = hier.next();
                    JsonNode arr = e.getValue();
                    if (arr.isArray()) {
                        for (JsonNode p : arr) allPermissions.add(p.asText());
                    }
                }
            }

            log.info("Found {} unique permission identifiers in JSON (including categories & hierarchy).", allPermissions.size());

            // 4) Ensure all PermissionEntity exist (skip wildcard patterns)
            for (String permName : allPermissions) {
                if (permName.contains("*")) {
                    log.debug("Skipping persistence of pattern/wildcard permission '{}" + "'", permName);
                    continue;
                }

                permissionRepository.findByName(permName).orElseGet(() -> {
                    PermissionEntity pe = new PermissionEntity(permName);
                    pe.setDescription(permName);
                    PermissionEntity saved = permissionRepository.save(pe);
                    log.info("Created permission: {} (id={})", saved.getName(), saved.getId());
                    return saved;
                });
            }

            // Build concrete permission name set (exclude wildcard patterns containing '*')
            Set<String> concretePermissionNames = permissionRepository.findAll().stream()
                    .map(PermissionEntity::getName)
                    .filter(name -> !name.contains("*"))
                    .collect(Collectors.toSet());

            log.info("Found {} concrete permissions in DB for pattern expansion.", concretePermissionNames.size());

            // 5) Ensure roles and mappings (with pattern expansion)
            if (rolePermissions.isObject()) {
                Iterator<java.util.Map.Entry<String, JsonNode>> roles = rolePermissions.fields();
                while (roles.hasNext()) {
                    java.util.Map.Entry<String, JsonNode> r = roles.next();
                    String roleName = r.getKey();
                    JsonNode roleNode = r.getValue();

                    RoleEntity roleEntity = roleRepository.findByName(roleName).orElseGet(() -> {
                        RoleEntity rr = new RoleEntity();
                        rr.setName(roleName);
                        rr.setPermissions(new HashSet<>());
                        RoleEntity saved = roleRepository.save(rr);
                        log.info("Created role: {} (id={})", saved.getName(), saved.getId());
                        return saved;
                    });

                    Set<PermissionEntity> current = roleEntity.getPermissions();
                    if (current == null) current = new HashSet<>();

                    // Build a set of existing permission names to compare by value (avoid relying on entity equals)
                    Set<String> currentNames = current.stream().map(PermissionEntity::getName).collect(Collectors.toSet());

                    JsonNode perms = roleNode.path("permissions");
                    if (perms.isArray()) {
                        for (JsonNode p : perms) {
                            String pName = p.asText();

                            // If pattern contains wildcard, expand against concretePermissionNames
                            if (pName.contains("*")) {
                                // convert wildcard to regex, escape dots
                                String regex = pName.replace(".", "\\.").replace("*", ".*");
                                Pattern pat = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
                                List<String> matches = concretePermissionNames.stream()
                                        .filter(name -> pat.matcher(name).matches())
                                        .collect(Collectors.toList());

                                for (String match : matches) {
                                    if (!currentNames.contains(match)) {
                                        PermissionEntity pe = permissionRepository.findByName(match).orElseThrow(() ->
                                                new IllegalStateException("Permission not found while mapping role pattern: " + match));
                                        current.add(pe);
                                        currentNames.add(match);
                                        log.debug("Expanded pattern '{}' -> mapped '{}' for role '{}" + "'", pName, match, roleName);
                                    }
                                }

                                continue;
                            }

                            // Non-pattern: map directly
                            PermissionEntity pe = permissionRepository.findByName(pName).orElseThrow(() ->
                                    new IllegalStateException("Permission not found while mapping role: " + pName));
                            if (!currentNames.contains(pName)) {
                                current.add(pe);
                                currentNames.add(pName);
                                log.debug("Mapping role '{}' -> permission '{}" + "'", roleName, pName);
                            }
                        }
                    }

                    roleEntity.setPermissions(current);
                    roleRepository.save(roleEntity);
                }
            }

            log.info("Role/permission initialization finished. roles={}, permissions={}", roleRepository.count(), permissionRepository.count());
        }
    }
}

