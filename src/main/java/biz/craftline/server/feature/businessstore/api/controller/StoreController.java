package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDetailsResponse;
import biz.craftline.server.feature.businessstore.api.dto.StoreInfo;
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
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * REST controller for managing stores.
 */
@Slf4j
@RequestMapping({"/api/stores", "/api/store"})
@RestController
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);


    private final StoreDTOMapper mapper;
    private final StoreService service;
    private final BusinessEntityService businessService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final ServicesOfferedByStoreService servicesOfferedByStoreService;
    private final ProductsOfferedByStoreService productsOfferedByStoreService;
    private final StoreOfferedServiceDTOMapper storeOfferedServiceDTOMapper;
    private final StoreOfferedProductDTOMapper storeOfferedProductDTOMapper;

    public StoreController(StoreDTOMapper mapper, StoreService service,
                           BusinessEntityService businessService,
                           EmployeeService employeeService,
                           CustomerService customerService,
                           OrderService orderService,
                           ServicesOfferedByStoreService servicesOfferedByStoreService,
                           ProductsOfferedByStoreService productsOfferedByStoreService,
                           StoreOfferedServiceDTOMapper storeOfferedServiceDTOMapper,
                           StoreOfferedProductDTOMapper storeOfferedProductDTOMapper) {
        this.mapper = mapper;
        this.service = service;
        this.businessService = businessService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.servicesOfferedByStoreService = servicesOfferedByStoreService;
        this.productsOfferedByStoreService = productsOfferedByStoreService;
        this.storeOfferedServiceDTOMapper = storeOfferedServiceDTOMapper;
        this.storeOfferedProductDTOMapper = storeOfferedProductDTOMapper;
    }

    /**
     * List all stores.
     */
    @Operation(summary = "List all stores", description = "Returns all stores.")
    @ApiResponse(responseCode = "200", description = "List of stores returned successfully.")
    @GetMapping
    public ResponseEntity<APIResponse<Map<String, Object>>> listStores(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "businessId", required = false) Long businessId,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "keyword", required = false) String keyword) {
        List<Store> stores = businessId != null
                ? service.findStoresByBusiness(businessId)
                : service.findAll();

        List<StoreDTO> dtoList = stores.stream()
                .filter(store -> status == null || store.getStatus() == status)
                .filter(store -> matchesKeyword(store, keyword))
                .map(mapper::toDTO)
                .toList();

        return APIResponse.success(toPagedResult(dtoList, page, size), "Stores retrieved successfully");
    }

    @Operation(summary = "List all stores", description = "Returns all stores.")
    @ApiResponse(responseCode = "200", description = "List of stores returned successfully.")
    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<StoreDTO>>> list() {
        List<Store> list = service.findAll();
        List<StoreDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    /**
     * List stores by business ID.
     */
    @Operation(summary = "Store details by store ID", description = "Returns  store")
    @ApiResponse(responseCode = "200", description = "store returned successfully.")
    @GetMapping("/{storeId}")
    public ResponseEntity<APIResponse<StoreDTO>> storeDetails(@PathVariable("storeId") long storeId) {
        logger.info("Store: {}", storeId);
        Store store = service.findById(storeId).orElse(null);
        if (store == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }
        return APIResponse.success(mapper.toDTO(store), "Store retrieved successfully");
    }

    @Operation(summary = "Store full details by store ID", description = "Returns store details with employees, products, services, customers and orders")
    @ApiResponse(responseCode = "200", description = "Store details returned successfully.")
    @GetMapping("/{storeId}/details")
    public ResponseEntity<APIResponse<StoreDetailsResponse>> storeFullDetails(@PathVariable("storeId") long storeId) {
        Store store = service.findById(storeId).orElse(null);
        if (store == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }

        List<Employee> employees = employeeService.getEmployeesByStoreId(storeId);
        List<Customer> customers = customerService.findByStoreId(storeId);
        List<Order> orders = orderService.getOrdersByStoreId(storeId);

        StoreDetailsResponse response = StoreDetailsResponse.builder()
                .info(buildStoreInfo(store))
                .employeeList(employees)
                .services(servicesOfferedByStoreService.findServicesByStoreId(storeId).orElse(List.of()).stream()
                        .map(storeOfferedServiceDTOMapper::toDTO)
                        .toList())
                .products(productsOfferedByStoreService.findProductsByStoreId(storeId).orElse(List.of()).stream()
                        .map(storeOfferedProductDTOMapper::toDTO)
                        .toList())
                .customers(customers)
                .orders(orders)
                .build();

        return APIResponse.success(response, "Store details retrieved successfully");
    }


    /**
     * Store Details by id.
     */
    @Operation(summary = "Store Details by store id", description = "Store Details")
    @ApiResponse(responseCode = "200", description = "Store Details returned successfully.")
    @GetMapping("/store-info/{storeId}")
    public ResponseEntity<APIResponse<StoreInfo>> storeDetailsById(@PathVariable("storeId") long storeId) {
        Store store = service.findById(storeId).orElse(null);
        if (store == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }

        StoreInfo response = buildStoreInfo(store);
        return APIResponse.success(response, "Store retrieved successfully");
    }

    /**
     * List stores by business ID.
     */
    @Operation(summary = "List stores by business ID", description = "Returns all stores for a given business.")
    @ApiResponse(responseCode = "200", description = "List of stores returned successfully.")
    @GetMapping("/list/{businessId}")
    public ResponseEntity<APIResponse<List<StoreDTO>>> list(@PathVariable("businessId") long businessId) {
        logger.info("businessId: {}", businessId);
        List<Store> list = service.findStoresByBusiness(businessId);
        List<StoreDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList, "Stores retrieved successfully");
    }

    /**
     * Search stores by keyword.
     */
    @Operation(summary = "Search stores", description = "Search stores by keyword.")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully.")
    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<StoreDTO>>> search(@RequestBody SearchRequest request) {
        List<Store> list = service.searchStores(request.keyword());
        List<StoreDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    /**
     * Add a new store.
     */
    @Operation(summary = "Add new store", description = "Creates a new store.")
    @ApiResponse(responseCode = "201", description = "Store created successfully.")
    @PostMapping
    public ResponseEntity<APIResponse<StoreDTO>> addStore(@RequestBody AddNewStoreRequest request) {
        Business business = null;
        if (request.getBusinessId() != null) {
            business = businessService.findById(request.getBusinessId()).orElse(null);
        }
        Store store = mapper.toDomain(request);
        store.setBusiness(business);
        Store savedStore = service.save(store);
        return APIResponse.success(
                mapper.toDTO(savedStore),
                "Store created successfully",
                HttpStatus.CREATED);
    }

    /**
     * Update an existing store.
     */
    @Operation(summary = "Update store", description = "Updates an existing store.")
    @ApiResponse(responseCode = "200", description = "Store updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<StoreDTO>> updateStore(
            @PathVariable("id") Long id,
            @RequestBody AddNewStoreRequest request) {
        // Check if store exists
        Store existingStore = service.findById(id).orElse(null);
        if (existingStore == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }

        // Get business if provided
        Business business = null;
        if (request.getBusinessId() != null) {
            business = businessService.findById(request.getBusinessId()).orElse(null);
        }

        // Update store fields
        Store storeToUpdate = mapper.toDomain(request);
        storeToUpdate.setId(id);
        storeToUpdate.setBusiness(business);
        
        Store updatedStore = service.save(storeToUpdate);
        return APIResponse.success(
                mapper.toDTO(updatedStore),
                "Store updated successfully");
    }

    /**
     * Delete a store by ID.
     */
    @Operation(summary = "Delete store", description = "Deletes a store by ID.")
    @ApiResponse(responseCode = "200", description = "Store deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteStore(@PathVariable("id") Long id) {
        // Check if store exists
        Store existingStore = service.findById(id).orElse(null);
        if (existingStore == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }

        service.deleteStoreById(id);
        return APIResponse.success("Store deleted successfully");
    }

    /**
     * Update store status.
     */
    @Operation(summary = "Update store status", description = "Updates the status of a store.")
    @ApiResponse(responseCode = "200", description = "Store status updated successfully.")
    @PostMapping("/update-status")
    public ResponseEntity<APIResponse<StoreDTO>> updateStoreStatus(@RequestBody StatusUpdateRequest request) {
        Store existingStore = service.findById(request.id()).orElse(null);
        if (existingStore == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }
        
        existingStore.setStatus(request.status());
        Store updatedStore = service.save(existingStore);
        return APIResponse.success(mapper.toDTO(updatedStore), "Store status updated successfully");
    }

    /**
     * Get store dashboard metrics.
     */
    @Operation(summary = "Get store metrics", description = "Returns dashboard metrics for a store.")
    @ApiResponse(responseCode = "200", description = "Store metrics returned successfully.")
    @GetMapping("/{storeId}/metrics")
    public ResponseEntity<APIResponse<StoreMetricsDTO>> getStoreMetrics(@PathVariable("storeId") long storeId) {
        Store store = service.findById(storeId).orElse(null);
        if (store == null) {
            return APIResponse.error("Store not found", HttpStatus.NOT_FOUND);
        }

        // TODO: Fetch actual metrics from various services (orders, inventory, employees)
        // For now, return placeholder metrics
        StoreMetricsDTO metrics = StoreMetricsDTO.builder()
                .todayRevenue(0.0)
                .todayOrders(0)
                .activeCustomers(0)
                .totalProducts(0)
                .lowStockItems(0)
                .pendingOrders(0)
                .monthlyRevenue(0.0)
                .monthlyGrowth(0.0)
                .totalEmployees(0)
                .build();

        return APIResponse.success(metrics, "Store metrics retrieved successfully");
    }

    private StoreInfo buildStoreInfo(Store store) {
        return StoreInfo.builder()
                .name(store.getStoreName())
                .description(store.getDescription())
                .address(store.getAddress())
                .category(store.getBusinessType() != null ? String.valueOf(store.getBusinessType()) : "General")
                .storeManager(store.getManager() != null ? store.getManager() : "Not assigned")
                .establishedDate(store.getCreatedAt() != null ? store.getCreatedAt().toLocalDate().toString() : "")
                .email(store.getEmail())
                .phone(store.getPhone())
                .build();
    }

    private boolean matchesKeyword(Store store, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return contains(store.getStoreName(), normalizedKeyword)
                || contains(store.getDescription(), normalizedKeyword)
                || contains(store.getAddress(), normalizedKeyword)
                || contains(store.getEmail(), normalizedKeyword)
                || contains(store.getPhone(), normalizedKeyword)
                || contains(store.getManager(), normalizedKeyword)
                || (store.getBusiness() != null && contains(store.getBusiness().getBusinessName(), normalizedKeyword));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private <T> Map<String, Object> toPagedResult(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int totalElements = items.size();
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", items.subList(fromIndex, toIndex));
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("currentPage", safePage);
        result.put("pageSize", safeSize);
        return result;
    }

}