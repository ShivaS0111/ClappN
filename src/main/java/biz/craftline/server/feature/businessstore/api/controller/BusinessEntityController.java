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
import biz.craftline.server.util.APIResponse;
import biz.craftline.server.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST controller for managing businesses.
 */
@RequestMapping("/api/business")
@RestController
public class BusinessEntityController {

    private final BusinessDTOMapper mapper;
    private final BusinessEntityService service;
    private final UserService userService;

    public BusinessEntityController(BusinessDTOMapper mapper, BusinessEntityService service, UserService userService) {
        this.mapper = mapper;
        this.service = service;
        this.userService = userService;
    }

    /**
     * List all businesses.
     */
    @Operation(summary = "List all businesses", description = "Returns all businesses.")
    @ApiResponse(responseCode = "200", description = "List of businesses returned successfully.")
    @GetMapping
    public ResponseEntity<APIResponse<Map<String, Object>>> listBusinesses(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "keyword", required = false) String keyword) {
        List<BusinessDTO> dtoList = service.findAll().stream()
                .filter(business -> status == null || Objects.equals(business.getStatus(), status))
                .filter(business -> matchesKeyword(business, keyword))
                .map(mapper::toDTO)
                .toList();

        return APIResponse.success(toPagedResult(dtoList, page, size), "Businesses retrieved successfully");
    }

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
    @PostMapping
    public ResponseEntity<APIResponse<BusinessDTO>> addBusiness(
            @Valid @RequestBody AddNewBusinessRequest request) {
        Business business = mapper.toDomain(request);
        business.setCreatedBy(getCurrentUserId());
        Business savedBusiness = service.save(business);
        return APIResponse.success(mapper.toDTO(savedBusiness));
    }

    /**
     * Update a new business.
     */
    @Operation(summary = "Update business", description = "Update business.")
    @ApiResponse(responseCode = "200", description = "Business updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<BusinessDTO>> updateBusiness(
            @Valid @RequestBody UpdateBusinessRequest request, @PathVariable("id") Long id) {
        Business businessM = mapper.toDomain(request);

        Business business = service.findById(id).orElseThrow(()-> new RuntimeException("Business not found:: " + id));
        business.setId(id);
        business.setCreatedBy(getCurrentUserId());

        Business savedBusiness = service.save(mapper.toUpdated(business, businessM));
        return APIResponse.success(mapper.toDTO(savedBusiness));
    }

    @Operation(summary = "Update business status", description = "Update business status.")
    @ApiResponse(responseCode = "200", description = "Business status updated successfully.")
    @PostMapping("/update-status")
    public ResponseEntity<APIResponse<BusinessDTO>> updateBusinessStatus(
            @Valid @RequestBody StatusUpdateRequest updateRequest) {
        Business business = service.findById(updateRequest.id()).orElseThrow(()-> new RuntimeException("Business not found:: " + updateRequest.id()));
        business.setStatus(updateRequest.status());
        Business savedBusiness = service.save(business);
        return APIResponse.success(mapper.toDTO(savedBusiness));
    }

    /**
     * Update a new business.
     */
    @Operation(summary = "GET business", description = "GET business.")
    @ApiResponse(responseCode = "200", description = "business listed successfully.")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<BusinessDTO>> getBusiness( @PathVariable("id") Long id) {
        Business business = service.findById(id).orElseThrow(()-> new RuntimeException("Business not found:: " + id));
        return APIResponse.success(mapper.toDTO(business));
    }

    private boolean matchesKeyword(Business business, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return contains(business.getBusinessName(), normalizedKeyword)
                || contains(business.getDescription(), normalizedKeyword)
                || contains(business.getContact(), normalizedKeyword)
                || contains(business.getEmail(), normalizedKeyword)
                || contains(business.getWebsite(), normalizedKeyword)
                || contains(business.getAddress(), normalizedKeyword);
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

    private Long getCurrentUserId() {
        String currentUsername = UserUtil.requireCurrentUsername();
        return userService.getUserByEmail(currentUsername)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}
