package biz.craftline.server.feature.usermanagement.infra.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RolePermissionInitializer {

    private final RolePermissionService rolePermissionService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            rolePermissionService.initializeFromResource();
        } catch (Exception ex) {
            log.error("Failed to initialize roles/permissions from resource", ex);
        }
    }
}
