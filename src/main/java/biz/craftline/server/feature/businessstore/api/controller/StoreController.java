package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.api.request.SearchRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing stores.
 */
@RequestMapping("/stores")
@RestController
public class StoreController {

    private final StoreDTOMapper mapper;
    private final StoreService service;
    private final BusinessEntityService businessService;

    public StoreController(StoreDTOMapper mapper, StoreService service, BusinessEntityService businessService) {
        this.mapper = mapper;
        this.service = service;
        this.businessService = businessService;
    }

    /**
     * List all stores.
     */
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
    @Operation(summary = "List stores by business ID", description = "Returns all stores for a given business.")
    @ApiResponse(responseCode = "200", description = "List of stores returned successfully.")
    @GetMapping("/list/{businessId}")
    public ResponseEntity<APIResponse<List<StoreDTO>>> list(@PathVariable("businessId") long businessId) {
        List<Store> list = service.findStoresByBusiness(businessId);
        List<StoreDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList);
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
    @ApiResponse(responseCode = "200", description = "Store created successfully.")
    @PostMapping("/add")
    public ResponseEntity<APIResponse<StoreDTO>> addStore(@Valid @RequestBody AddNewStoreRequest request) {
        Business business = null;
        if (request.getBusinessId() != null) {
            business = businessService.findById(request.getBusinessId()).orElse(null);
        }
        Store store = mapper.toDomain(request);
        store.setBusiness(business);
        Store savedStore = service.save(store);
        return APIResponse.success(mapper.toDTO(savedStore));
    }

}