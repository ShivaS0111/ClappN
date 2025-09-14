package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedServiceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
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
@RequestMapping("/store-service")
@RestController
public class StoreOfferedServiceController {
    private final StoreOfferedServiceDTOMapper serviceMapper;
    private final StoreItemPriceDTOMapper priceMapper;
    private final ServicesOfferedByStoreService storeOfferedService;
    private final StoreItemPriceService priceHandleService;

    @GetMapping("/list/{storeId}")
    public ResponseEntity<APIResponse<List<StoreOfferedServiceDTO>>> list(@PathVariable("storeId") Long storeId) {
        Optional<List<StoreOfferedService>> list = storeOfferedService.findServicesByStoreId(storeId);
        List<StoreOfferedServiceDTO> dtoList = list.orElseThrow().stream()
                .map(serviceMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList, "Store services retrieved successfully");
    }

    @PostMapping("/save")
    public ResponseEntity<APIResponse<StoreOfferedServiceDTO>> save(@RequestBody AddNewStoreOfferedServiceRequest req) {
        StoreOfferedService service = storeOfferedService.save(serviceMapper.toDomain(req));
        return APIResponse.success(serviceMapper.toDTO(service), "Service added to store successfully", HttpStatus.CREATED);
    }

    @GetMapping("/service-price-list/{serviceId}")
    public ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> priceList(
            @PathVariable("serviceId") Long serviceId
    ) {
        List<StoreItemPrice> list = priceHandleService.findAllByServiceId(serviceId);
        List<StoreItemPriceDTO> dtoList = list.stream()
                .map(priceMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList);
    }
}
