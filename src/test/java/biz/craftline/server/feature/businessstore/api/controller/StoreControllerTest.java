package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreMetricsDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedProductDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.api.request.SearchRequest;
import biz.craftline.server.feature.businessstore.api.request.StatusUpdateRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.domain.service.CustomerService;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoreControllerTest {

    @Mock private StoreDTOMapper mapper;
    @Mock private StoreService service;
    @Mock private BusinessEntityService businessService;
    @Mock private EmployeeService employeeService;
    @Mock private CustomerService customerService;
    @Mock private OrderService orderService;
    @Mock private ServicesOfferedByStoreService servicesOfferedByStoreService;
    @Mock private ProductsOfferedByStoreService productsOfferedByStoreService;
    @Mock private StoreOfferedServiceDTOMapper storeOfferedServiceDTOMapper;
    @Mock private StoreOfferedProductDTOMapper storeOfferedProductDTOMapper;
    @Mock private StoreInventoryService storeInventoryService;

    private StoreController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new StoreController(mapper, service, businessService, employeeService,
                customerService, orderService, servicesOfferedByStoreService,
                productsOfferedByStoreService, storeOfferedServiceDTOMapper,
                storeOfferedProductDTOMapper, storeInventoryService);
    }

    private Store store(long id, String name) {
        return Store.builder().id(id).storeName(name).status(1).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void listStores_pagingAndFilters() {
        Store s1 = store(1L, "Alpha");
        Store s2 = store(2L, "Beta");
        when(service.findAll()).thenReturn(List.of(s1, s2));
        when(mapper.toDTO(any())).thenAnswer(inv -> StoreDTO.builder()
                .id(((Store) inv.getArgument(0)).getId())
                .storeName(((Store) inv.getArgument(0)).getStoreName())
                .build());

        Map<String, Object> data = controller.listStores(0, 1, null, null, null).getBody().getData();
        assertEquals(1, ((List<?>) data.get("content")).size());
        assertEquals(2, data.get("totalElements"));

        when(service.findStoresByBusiness(10L)).thenReturn(List.of(s1));
        assertEquals(1, controller.listStores(0, 10, 10L, 1, "alp").getBody().getData().get("totalElements"));
    }

    @Test
    void list_and_listByBusiness() {
        Store s = store(1L, "S");
        when(service.findAll()).thenReturn(List.of(s));
        when(service.findStoresByBusiness(5L)).thenReturn(List.of(s));
        when(mapper.toDTO(s)).thenReturn(StoreDTO.builder().id(1L).build());

        assertEquals(1, controller.list().getBody().getData().size());
        assertEquals(1, controller.list(5L).getBody().getData().size());
    }

    @Test
    void storeDetails_foundAndMissing() {
        Store s = store(1L, "S");
        when(service.findById(1L)).thenReturn(Optional.of(s));
        when(service.findById(9L)).thenReturn(Optional.empty());
        when(mapper.toDTO(s)).thenReturn(StoreDTO.builder().id(1L).build());

        assertEquals(HttpStatus.OK, controller.storeDetails(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.storeDetails(9L).getStatusCode());
    }

    @Test
    void storeFullDetails_and_storeInfo() {
        Store s = store(1L, "S");
        when(service.findById(1L)).thenReturn(Optional.of(s));
        when(service.findById(2L)).thenReturn(Optional.empty());
        when(employeeService.getEmployeesByStoreId(1L)).thenReturn(List.of(new Employee()));
        when(customerService.findByStoreId(1L)).thenReturn(List.of());
        when(orderService.getOrdersByStoreId(1L)).thenReturn(List.of());
        when(servicesOfferedByStoreService.findServicesByStoreId(1L)).thenReturn(Optional.of(List.of()));
        when(productsOfferedByStoreService.findProductsByStoreId(1L)).thenReturn(Optional.of(List.of()));

        assertEquals(HttpStatus.OK, controller.storeFullDetails(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.storeFullDetails(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.storeDetailsById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.storeDetailsById(2L).getStatusCode());
    }

    @Test
    void search_add_update_delete_status() {
        Store s = store(1L, "S");
        when(service.searchStores("kw")).thenReturn(List.of(s));
        when(mapper.toDTO(s)).thenReturn(StoreDTO.builder().id(1L).build());
        assertEquals(1, controller.search(new SearchRequest("kw")).getBody().getData().size());

        AddNewStoreRequest req = new AddNewStoreRequest();
        req.setBusinessId(10L);
        Business biz = Business.builder().id(10L).build();
        when(businessService.findById(10L)).thenReturn(Optional.of(biz));
        when(mapper.toDomain(req)).thenReturn(Store.builder().storeName("N").build());
        when(service.save(any())).thenReturn(s);
        when(mapper.toDTO(s)).thenReturn(StoreDTO.builder().id(1L).build());
        assertEquals(HttpStatus.CREATED, controller.addStore(req).getStatusCode());

        when(service.findById(1L)).thenReturn(Optional.of(s));
        when(service.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.OK, controller.updateStore(1L, req).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateStore(99L, req).getStatusCode());
        assertEquals(HttpStatus.OK, controller.deleteStore(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteStore(99L).getStatusCode());

        when(service.save(any())).thenReturn(s);
        assertEquals(HttpStatus.OK, controller.updateStoreStatus(new StatusUpdateRequest(1L, 0)).getStatusCode());
        assertThrows(IllegalArgumentException.class, () -> controller.updateStoreStatus(new StatusUpdateRequest(null, 0)));
        assertThrows(IllegalArgumentException.class, () -> controller.updateStoreStatus(new StatusUpdateRequest(1L, null)));
    }

    @Test
    void getStoreMetrics() {
        Store s = store(1L, "S");
        when(service.findById(1L)).thenReturn(Optional.of(s));
        when(service.findById(2L)).thenReturn(Optional.empty());
        Customer active = Customer.builder().status(1).build();
        Customer inactive = Customer.builder().status(0).build();
        when(employeeService.getEmployeesByStoreId(1L)).thenReturn(List.of(new Employee(), new Employee()));
        when(customerService.findByStoreId(1L)).thenReturn(List.of(active, inactive));
        Order today = new Order();
        today.setStatus("COMPLETED");
        today.setTotalAmount(100.0);
        today.setOrderDate(LocalDateTime.now());
        Order pending = new Order();
        pending.setStatus("CREATED");
        pending.setOrderDate(LocalDateTime.now().minusDays(2));
        when(orderService.getOrdersByStoreId(1L)).thenReturn(List.of(today, pending));
        when(productsOfferedByStoreService.findProductsByStoreId(1L)).thenReturn(Optional.of(List.of()));
        when(storeInventoryService.countLowStockByStoreId(1L, 5)).thenReturn(3L);

        StoreMetricsDTO metrics = controller.getStoreMetrics(1L).getBody().getData();
        assertEquals(2, metrics.getTotalEmployees());
        assertEquals(1, metrics.getActiveCustomers());
        assertEquals(1, metrics.getPendingOrders());
        assertEquals(3, metrics.getLowStockItems());
        assertEquals(HttpStatus.NOT_FOUND, controller.getStoreMetrics(2L).getStatusCode());
    }
}
