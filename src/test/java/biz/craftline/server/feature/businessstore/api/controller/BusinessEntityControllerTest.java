package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.mapper.BusinessDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.api.request.SearchRequest;
import biz.craftline.server.feature.businessstore.api.request.StatusUpdateRequest;
import biz.craftline.server.feature.businessstore.api.request.UpdateBusinessRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BusinessEntityControllerTest {

    @Mock private BusinessDTOMapper mapper;
    @Mock private BusinessEntityService service;
    @Mock private UserService userService;

    private BusinessEntityController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BusinessEntityController(mapper, service, userService);
    }

    private Business biz(long id, String name) {
        return Business.builder().id(id).businessName(name).status(1).build();
    }

    @Test
    void listAndSearch() {
        Business b = biz(1L, "Acme");
        when(service.findAll()).thenReturn(List.of(b));
        when(mapper.toDTO(b)).thenReturn(BusinessDTO.builder().id(1L).businessName("Acme").status(1).build());

        Map<String, Object> page = controller.listBusinesses(0, 10, null, null).getBody().getData();
        assertEquals(1, page.get("totalElements"));
        assertEquals(1, controller.list().getBody().getData().size());

        when(service.search("ac")).thenReturn(List.of(b));
        assertEquals(1, controller.search(new SearchRequest("ac")).getBody().getData().size());
    }

    @Test
    void getBusiness() {
        Business b = biz(1L, "Acme");
        when(service.findById(1L)).thenReturn(Optional.of(b));
        when(mapper.toDTO(b)).thenReturn(BusinessDTO.builder().id(1L).build());
        assertNotNull(controller.getBusiness(1L).getBody().getData());
        when(service.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> controller.getBusiness(9L));
    }

    @Test
    void addAndUpdateBusiness() {
        Business b = biz(1L, "Acme");
        BusinessDTO dto = BusinessDTO.builder().id(1L).businessName("Acme").build();
        AddNewBusinessRequest addReq = new AddNewBusinessRequest();
        when(mapper.toDomain(addReq)).thenReturn(Business.builder().businessName("New").build());

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::requireCurrentUsername).thenReturn("admin@test.com");
            User admin = new User();
            admin.setId(99L);
            when(userService.getUserByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(service.createBusinessWithOwner(any(), any(), any(), any(), any())).thenReturn(b);
            when(mapper.toDTO(b)).thenReturn(dto);
            assertNotNull(controller.addBusiness(addReq).getBody().getData());

            UpdateBusinessRequest upd = new UpdateBusinessRequest();
            when(service.findById(1L)).thenReturn(Optional.of(b));
            when(mapper.toDomain(upd)).thenReturn(Business.builder().businessName("Upd").build());
            when(mapper.toUpdated(any(), any())).thenReturn(b);
            when(service.save(b)).thenReturn(b);
            assertNotNull(controller.updateBusiness(upd, 1L).getBody().getData());

            when(service.findById(1L)).thenReturn(Optional.of(b));
            when(service.save(any())).thenReturn(b);
            assertNotNull(controller.updateBusinessStatus(new StatusUpdateRequest(1L, 0)).getBody().getData());
        }
    }
}
