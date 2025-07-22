package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedServiceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreProductPriceService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequestMapping("/store-service")
@RestController
public class StoreOfferedServiceController {


    @Autowired
    StoreOfferedServiceDTOMapper serviceMapper;

    @Autowired
    StoreItemPriceDTOMapper priceMapper;

    @Autowired
    private ServicesOfferedByStoreService storeOfferedService;

    @Autowired
    private StoreProductPriceService priceHandleService;

    @GetMapping("/list/{storeId}")
    public ResponseEntity<APIResponse<List<StoreOfferedServiceDTO>>> list(@PathVariable("storeId") Long storeId) {
        //if( storeId==null ) throw new
        Optional<List<StoreOfferedService>> list = storeOfferedService.findServicesByStoreId(storeId);
        List<StoreOfferedServiceDTO> dtoList = list.orElseThrow().stream()
                .map(serviceMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList);
    }


    @PostMapping("/save")
    public ResponseEntity<APIResponse<String>> save(@RequestBody AddNewStoreOfferedServiceRequest req) {
        storeOfferedService.save( serviceMapper.toDomain(req));
        return APIResponse.success("success");
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
