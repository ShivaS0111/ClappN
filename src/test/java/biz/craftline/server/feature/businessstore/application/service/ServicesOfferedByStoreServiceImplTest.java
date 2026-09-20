package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicesOfferedByStoreServiceImplTest {
    @Mock private ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    @Mock private StoreOfferedServiceEntityMapper mapper;
    @Mock private BusinessServicesJpaRepository businessServicesJpaRepository;
    @Mock private SecurityContextService securityContextService;
    @Mock private UserService userService;
    @Mock private StoreItemPriceService storeItemPriceService;
    @InjectMocks private ServicesOfferedByStoreServiceImpl service;

    private MockedStatic<UserUtil> userUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(securityContextService).validateStoreAccess(any());
        userUtil = mockStatic(UserUtil.class);
        userUtil.when(UserUtil::requireCurrentUsername).thenReturn("owner@clapp.test");
        User user = new User();
        user.setId(99L);
        when(userService.getUserByEmail("owner@clapp.test")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        if (userUtil != null) {
            userUtil.close();
        }
    }

    @Test
    void deleteStoreServiceById_CallsRepository() {
        Long id = 1L;
        StoreOfferedServiceEntity entity = new StoreOfferedServiceEntity();
        entity.setId(id);
        entity.setStoreId(10L);
        when(servicesOfferedByStoreRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(
                StoreOfferedService.builder().id(id).storeId(10L).build());
        when(storeItemPriceService.findByServiceId(id)).thenReturn(Optional.empty());

        service.deleteStoreServiceById(id);
        verify(servicesOfferedByStoreRepository).deleteStoreServiceById(id);
    }

    @Test
    void findServicesByStoreId_ReturnsServiceList() {
        Long storeId = 1L;
        StoreOfferedServiceEntity entity1 = new StoreOfferedServiceEntity();
        StoreOfferedServiceEntity entity2 = new StoreOfferedServiceEntity();
        StoreOfferedService service1 = StoreOfferedService.builder().id(1L).storeId(storeId).aliasName("A").build();
        StoreOfferedService service2 = StoreOfferedService.builder().id(2L).storeId(storeId).aliasName("B").build();
        when(servicesOfferedByStoreRepository.findByStoreId(storeId)).thenReturn(Optional.of(Arrays.asList(entity1, entity2)));
        when(mapper.toDomain(entity1)).thenReturn(service1);
        when(mapper.toDomain(entity2)).thenReturn(service2);
        Optional<List<StoreOfferedService>> result = service.findServicesByStoreId(storeId);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void save_ReturnsSavedService() {
        StoreOfferedService domain = StoreOfferedService.builder().id(1L).storeId(1L).aliasName("A").build();
        StoreOfferedServiceEntity entity = new StoreOfferedServiceEntity();
        StoreOfferedServiceEntity savedEntity = new StoreOfferedServiceEntity();
        StoreOfferedService savedDomain = StoreOfferedService.builder().id(1L).storeId(1L).aliasName("A").build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(servicesOfferedByStoreRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);
        assertEquals(savedDomain, service.save(domain));
    }
}
