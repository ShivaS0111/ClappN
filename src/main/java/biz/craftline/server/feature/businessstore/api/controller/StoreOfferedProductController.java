package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedProductDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedProductRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequestMapping("/api/store-product")
@RestController
public class StoreOfferedProductController {

    private final StoreOfferedProductDTOMapper productMapper;
    private final StoreItemPriceDTOMapper priceMapper;
    private final ProductsOfferedByStoreService storeOfferedProductService;
    private final StoreItemPriceService priceHandleService;


    @GetMapping
    public ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> list() {
        List<StoreOfferedProductDTO> dtoList = storeOfferedProductService.findAll().stream()
                .map( productMapper::toDTO).toList();
        return APIResponse.success(dtoList, "Store products retrieved successfully");
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> search(
            @PathVariable("searchTerm") String searchTerm) {
        List<StoreOfferedProduct> list = storeOfferedProductService.searchProductByKeyword(searchTerm);
        List<StoreOfferedProductDTO> dtoList = list.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList, "Store products retrieved successfully");
    }

    @GetMapping("/search/{searchTerm}/{storeId}")
    public ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> searchProductByStoreIdAndKeyword(
            @PathVariable("searchTerm") String searchTerm, @PathVariable("storeId") Long storeId) {
        List<StoreOfferedProduct> list = storeOfferedProductService.searchProductByStoreIdAndKeyword(storeId, searchTerm);
        List<StoreOfferedProductDTO> dtoList = list.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList, "Store products retrieved successfully");
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> storeOfferedProducts(
            @PathVariable("storeId") Long storeId) {
        Optional<List<StoreOfferedProduct>> list = storeOfferedProductService.findProductsByStoreId(storeId);
        List<StoreOfferedProductDTO> dtoList = list.orElseThrow().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList, "Store products retrieved successfully");
    }

    @PostMapping("/save")
    public ResponseEntity<APIResponse<StoreOfferedProductDTO>> save(@RequestBody AddNewStoreOfferedProductRequest req) {
        StoreOfferedProduct product = storeOfferedProductService.save(productMapper.toDomain(req));
        return APIResponse.success(productMapper.toDTO(product),
                "Product added to store successfully",
                HttpStatus.CREATED);
    }

    @PostMapping("/add-all")
    public ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> save(
            @RequestBody List<AddNewStoreOfferedProductRequest> requests) {
        if(requests==null || requests.isEmpty()) return APIResponse.badRequest("Invalid request");

        List<StoreOfferedProduct> product = storeOfferedProductService.save(requests.stream()
                .map(productMapper::toDomain)
                .toList());
        return APIResponse.success(
                product.stream().map(productMapper::toDTO).toList(),
                "Products added to store successfully",
                HttpStatus.CREATED
        );
    }

    @GetMapping("/product-price-list/{productId}")
    public ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> priceList(
            @PathVariable("productId") Long lotId
    ) {
        List<StoreItemPrice> list = priceHandleService.findAllByLotId(lotId);
        List<StoreItemPriceDTO> dtoList = list.stream()
                .map(priceMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList);
    }

}
