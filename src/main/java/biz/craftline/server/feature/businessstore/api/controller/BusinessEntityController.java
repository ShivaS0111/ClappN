package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.mapper.BusinessDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.api.request.SearchRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing businesses.
 */
@RequestMapping("/business")
@RestController
public class BusinessEntityController {

    private final BusinessDTOMapper mapper;
    private final BusinessEntityService service;

    public BusinessEntityController(BusinessDTOMapper mapper, BusinessEntityService service) {
        this.mapper = mapper;
        this.service = service;
    }

    /**
     * List all businesses.
     */
    @Operation(summary = "List all businesses", description = "Returns all businesses.")
    @ApiResponse(responseCode = "200", description = "List of businesses returned successfully.")
    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessDTO>>> list() {
        List<Business> list = service.findAll();
        List<BusinessDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    /**
     * Search businesses by keyword.
     */
    @Operation(summary = "Search businesses", description = "Search businesses by keyword.")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully.")
    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessDTO>>> search(@RequestBody SearchRequest request) {
        List<Business> list = service.search(request.keyword());
        List<BusinessDTO> dtoList = list.stream().map(mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    /**
     * Add a new business.
     */
    @Operation(summary = "Add new business", description = "Creates a new business.")
    @ApiResponse(responseCode = "200", description = "Business created successfully.")
    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessDTO>> addBusiness(
            @Valid @RequestBody AddNewBusinessRequest request) {
        Business business = mapper.toDomain(request);
        Business savedBusiness = service.save(business);
        return APIResponse.success(mapper.toDTO(savedBusiness));
    }
}
