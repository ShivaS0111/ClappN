package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.util.APIResponse;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/store")
@RestController
public class StoreItemPriceController {

    @Autowired
    StoreItemPriceService service;

    @Autowired
    StoreItemPriceDTOMapper mapper;


    @PostMapping("/lot/price/add")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> addStoreItemLotPrice(
            @RequestBody AddStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateLotPrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Lot price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @PostMapping("/lot/price/update")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> updateProductLotPrice(
            @RequestBody UpdateStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateLotPrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Lot price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @GetMapping("/lot/{lotId}/price")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> findPriceByLotId(
            @PathVariable("lotId") Long lotId) {

        StoreItemPrice price = service.findByLotId(lotId)
                .orElseThrow(() -> new RuntimeException("lotid price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @GetMapping("/lot/{lotId}/prices")
    public ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> findPricesByLotId(
            @PathVariable("lotId") Long lotId) {

        List<StoreItemPriceDTO> prices = service.findAllByLotId(lotId).stream().map(mapper::toDTO).toList();
        return APIResponse.success(prices);
    }






    @PostMapping("/service/price/add")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> addStoreItemServicePrice(
            @RequestBody AddStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateServicePrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @PostMapping("/service/price/update")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> updatePrice(
            @RequestBody UpdateStoreItemPriceRequest dto) {

        StoreItemPrice price = service.updateServicePrice(mapper.toDomain(dto))
                .orElseThrow(() -> new RuntimeException("Service price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @GetMapping("/service/{serviceId}/price")
    public ResponseEntity<APIResponse<StoreItemPriceDTO>> getServicePrice(
            @PathVariable("serviceId") Long serviceId) {

        StoreItemPrice price = service.findByServiceId(serviceId)
                .orElseThrow(() -> new RuntimeException("serviceId price not found"));
        StoreItemPriceDTO priceDTO = mapper.toDTO(price);
        return APIResponse.success(priceDTO);
    }

    @GetMapping("/service/{serviceId}/prices")
    public ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> getServicePrices(
            @PathVariable("serviceId") Long serviceId) {
        System.out.println("===>Serviceid: "+ serviceId);
        List<StoreItemPrice> prices = service.findAllByServiceId(serviceId);
        List<StoreItemPriceDTO> priceDTO = prices.stream()
                .map(price->mapper.toDTO(price))
                .toList();
        return APIResponse.success(priceDTO);
    }

}
