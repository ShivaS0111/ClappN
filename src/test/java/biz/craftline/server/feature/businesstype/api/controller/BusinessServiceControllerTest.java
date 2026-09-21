package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchServiceByBusinessRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessServiceControllerTest {

    @Mock private BusinessServiceDTOMapper mapper;
    @Mock private BusinessServicesService service;
    @Mock private BusinessTypeService businessTypeService;
    @InjectMocks private BusinessServiceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getById_success() {
        BusinessService bs = BusinessService.builder().id(1L).serviceName("Cut").build();
        when(service.findById(1L)).thenReturn(Optional.of(bs));
        when(mapper.toDTO(bs)).thenReturn(BusinessServiceDTO.builder().id(1L).name("Cut").build());

        ResponseEntity<APIResponse<BusinessServiceDTO>> response = controller.getById(1L);
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void getById_missing() {
        when(service.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> controller.getById(1L));
    }

    @Test
    void list_success() {
        when(service.findAll()).thenReturn(List.of(BusinessService.builder().id(1L).build()));
        when(mapper.toDTO(any())).thenReturn(new BusinessServiceDTO());
        assertEquals(1, controller.list().getBody().getData().size());
    }

    @Test
    void listByBusinessType() {
        when(service.findByBusinessTypeId(2L)).thenReturn(List.of());
        assertTrue(controller.listByBusinessType(2L).getBody().getData().isEmpty());
    }

    @Test
    void search() {
        when(service.findBySearch("cut")).thenReturn(List.of(BusinessService.builder().id(1L).build()));
        when(mapper.toDTO(any())).thenReturn(new BusinessServiceDTO());
        assertEquals(1, controller.search(new SearchRequest("cut")).getBody().getData().size());
    }

    @Test
    void searchByBusiness() {
        when(service.searchByKeywordAndBusinessType(2L, "k")).thenReturn(List.of());
        assertTrue(controller.searchServiceByBusiness(
                new SearchServiceByBusinessRequest("k", 2L)).getBody().getData().isEmpty());
    }

    @Test
    void add_success() {
        AddNewBusinessServiceRequest req = mock(AddNewBusinessServiceRequest.class);
        BusinessService domain = BusinessService.builder().serviceName("Cut").build();
        when(mapper.toDomain(req)).thenReturn(domain);
        when(service.save(domain)).thenReturn(BusinessService.builder().id(8L).build());
        when(mapper.toDTO(any())).thenReturn(BusinessServiceDTO.builder().id(8L).build());
        assertEquals(8L, controller.add(req).getBody().getData().getId());
    }

    @Test
    void update_success() {
        AddNewBusinessServiceRequest req = mock(AddNewBusinessServiceRequest.class);
        when(mapper.toDomain(req)).thenReturn(BusinessService.builder().serviceName("Cut").build());
        when(service.update(any())).thenReturn(BusinessService.builder().id(4L).build());
        when(mapper.toDTO(any())).thenReturn(BusinessServiceDTO.builder().id(4L).build());
        assertEquals(4L, controller.update(4L, req).getBody().getData().getId());
    }

    @Test
    void addAll_success() {
        AddNewBusinessServiceRequest req = mock(AddNewBusinessServiceRequest.class);
        when(req.getBusinessTypeId()).thenReturn(2L);
        when(businessTypeService.findAllByIds(List.of(2L))).thenReturn(List.of(
                BusinessType.builder().id(2L).build()));
        when(mapper.toDomain(req)).thenReturn(BusinessService.builder().serviceName("Cut").build());
        when(service.save(anyList())).thenReturn(List.of(BusinessService.builder().id(6L).build()));
        when(mapper.toDTO(any())).thenReturn(BusinessServiceDTO.builder().id(6L).build());

        assertEquals(1, controller.addAll(List.of(req)).getBody().getData().size());
    }

    @Test
    void addAll2_success() {
        AddNewBusinessServiceRequest req = mock(AddNewBusinessServiceRequest.class);
        when(req.getBusinessTypeId()).thenReturn(2L);
        when(businessTypeService.findAllByIds(List.of(2L))).thenReturn(List.of(
                BusinessType.builder().id(2L).build()));
        when(mapper.toDomain(req)).thenReturn(BusinessService.builder().serviceName("Cut").build());
        when(service.save(any(BusinessService.class))).thenReturn(BusinessService.builder().id(7L).build());
        when(mapper.toDTO(any())).thenReturn(BusinessServiceDTO.builder().id(7L).build());

        assertEquals(1, controller.addAll2(List.of(req)).getBody().getData().size());
    }

    @Test
    void addAll1_fromDtoList() {
        BusinessServiceDTO dto = BusinessServiceDTO.builder().name("Cut").build();
        when(mapper.toDomain(dto)).thenReturn(BusinessService.builder().serviceName("Cut").build());
        when(service.save(anyList())).thenReturn(List.of(BusinessService.builder().id(8L).build()));
        when(mapper.toDTO(any())).thenReturn(BusinessServiceDTO.builder().id(8L).build());

        assertEquals(1, controller.add(List.of(dto)).getBody().getData().size());
    }
}
