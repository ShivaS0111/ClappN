package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreProductPriceService;
import biz.craftline.server.util.APIResponse;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/store-item")
public class StoreItemPriceController {

    @Autowired
    StoreProductPriceService service;

    @Autowired
    StoreItemPriceDTOMapper mapper;

    @PostMapping("/service-price/update")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> updatePrice(
            @RequestBody UpdateStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateServicePrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @PostMapping("/product-lot-price/update")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> updateProductLotPrice(
            @RequestBody UpdateStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateProductLotPrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @PostMapping("/product-price/update")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> updateProductPrice(
            @RequestBody UpdateStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateProductPrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @GetMapping("/service-price/{serviceId}")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> getServicePrice(
            @PathParam("serviceId") Long serviceId) {

        StoreItemPrice price = service.findByServiceId(serviceId)
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    /*
    @GetMapping("/product-price/{productId}")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> getProductPrice(
            @PathParam("productId") Long productId) {

        StoreItemPrice price = service.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("productId price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }*/

    @GetMapping("/lot-product-price/{lotId}")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> findByProductLotId(
            @PathParam("lotId") Long lotId) {

        StoreItemPrice price = service.findByProductLotId(lotId)
                .orElseThrow(() -> new RuntimeException("productId price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

}
