package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreServicePriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreServicePriceDTOMapper;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreServicePriceHandleService;
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
    StoreServicePriceDTOMapper priceMapper;

    @Autowired
    private ServicesOfferedByStoreService storeOfferedService;

    @Autowired
    private StoreServicePriceHandleService priceHandleService;

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
    public ResponseEntity<APIResponse<String>> save(@RequestBody StoreOfferedServiceDTO dto) {
        storeOfferedService.save( serviceMapper.toDomain(dto));
        return APIResponse.success("success");
    }

    @PutMapping("/update")
    public ResponseEntity<APIResponse<String>> save(@RequestBody StoreServicePriceDTO dto) {
        priceHandleService.save( priceMapper.toDomain(dto));
        return APIResponse.success("success");
    }

    @GetMapping("/price-list/{serviceId}")
    public ResponseEntity<APIResponse<List<StoreServicePriceDTO>>> priceList(@PathVariable("serviceId") Long serviceId) {
        List<StoreServicePrice> list = priceHandleService.findAll(serviceId);
        List<StoreServicePriceDTO> dtoList = list.stream()
                .map(priceMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList);
    }

}
