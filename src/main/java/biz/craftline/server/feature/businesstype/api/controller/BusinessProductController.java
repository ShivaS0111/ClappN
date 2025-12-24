package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessProductDTOMapper;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchServiceByBusinessRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.util.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/business-product")
public class BusinessProductController {

    @Autowired
    private BusinessProductDTOMapper mapper;

    @Autowired
    private BusinessProductsService service;

    @Autowired
    private BusinessTypeService businessTypeService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<APIResponse<BusinessProductDTO>> getById(@PathVariable Long serviceId) {
        BusinessProduct bs = service.findById(serviceId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid Business Service ID: " + serviceId)
                );
        if (bs == null) {
            return APIResponse.success(null);
        }
        return APIResponse.success(mapper.toDTO(bs));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable Long serviceId) {
        service.deleteProductById(serviceId);
        return APIResponse.success("Deleted successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> list() {
        List<BusinessProduct> list = service.findAll();
        return APIResponse.success(convertToDTOList(list));
    }

    @GetMapping("/listByBusinessType/{businessTypeId}")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> listByBusinessType(@PathVariable Long businessTypeId) {
        List<BusinessProduct> list = service.findByBusinessTypeId(businessTypeId);
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> search(
            @RequestBody SearchRequest search) {
        List<BusinessProduct> list = service.findBySearch(search.keyword());
        System.out.println("===>Keyword: " + search.keyword() + ", resp:  " + list);
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search-by-business-type")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> searchServiceByBusinessType(
            @RequestBody SearchServiceByBusinessRequest request) {
        List<BusinessProduct> list = service.findByBusinessTypeIdAndSearch(request.business_type_id(), request.keyword());
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessProductDTO>> add(@RequestBody AddNewBusinessProductRequest dto) {
        BusinessProduct bs = service.save(mapper.toDomain(dto));
        return APIResponse.success(mapper.toDTO(bs));
    }

    @PostMapping("/add-all")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> addAll(
            @RequestBody List<AddNewBusinessProductRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return APIResponse.success(Collections.emptyList());
        }

        Set<Long> categoryIds = requests.stream()
                .flatMap(r -> (r.getCategories() != null ?
                        categoryService.findAllByIds(r.getCategories()).stream()
                                .map(Category::getId)
                        : Stream.empty()))
                .collect(Collectors.toSet());

        Map<Long, Category> categoryMap = categoryService.findAllByIds(categoryIds.stream().toList())
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<BusinessProduct> products = requests.stream()
                .map(request -> {
                    BusinessProduct product = mapper.toDomain(request);
                    List<Category> matchedCategories = request.getCategories().stream()
                            .map(categoryMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    product.setCategories(matchedCategories);
                    return product;
                })
                .toList();

        List<BusinessProduct> savedProducts = service.save(products);

        List<BusinessProductDTO> dtos = savedProducts.stream()
                .map(mapper::toDTO)
                .toList();

        return APIResponse.success(dtos);

    }

    @PostMapping("/add-all1")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> add(@RequestBody List<BusinessProductDTO> list) {
        List<BusinessProduct> services = list.stream().map(item -> mapper.toDomain(item)).toList();
        return APIResponse.success(convertToDTOList(service.save(services)));
    }

    private List<BusinessProductDTO> convertToDTOList(List<BusinessProduct> list) {
        return list.parallelStream().map(mapper::toDTO).toList();
    }
}
