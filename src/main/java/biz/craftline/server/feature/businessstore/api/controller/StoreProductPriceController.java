package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreProductPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreProductPriceDTOMapper;
import biz.craftline.server.feature.businessstore.domain.model.StoreProductPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreProductPriceService;
import biz.craftline.server.util.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class StoreProductPriceController {

    @Autowired
    StoreProductPriceService service;

    @Autowired
    StoreProductPriceDTOMapper mapper;

//    @PostMapping("/store-product-price/update")
//    StoreProductPrice update(StoreProductPrice entity){
//
//    }
//
//    @GetMapping("/list/{storeId}")
//    List<StoreProductPrice> findAllByProductIdAndProductType(Long productId, Long productType);


    @GetMapping("/{id}/price")
    public ResponseEntity<APIResponse<StoreProductPriceDTO>> getPrice(
            @PathVariable Long id,
            @RequestParam String countryCode,
            @RequestParam String currency) {

        StoreProductPrice productPrice= service.getConvertedPrice(id, countryCode, currency)
                .orElseThrow(() -> new RuntimeException("No price for product and country"));

        return APIResponse.success( mapper.toDTO(productPrice) );
    }

    /*private BigDecimal convert(BigDecimal price, String from, String to) {
        String key = from + "-" + to;
        CurrencyRate rate = rateRepo.findById(key)
                .orElseThrow(() -> new RuntimeException("Rate not found"));
        return price.multiply(rate.getRate());
    }*/
}
