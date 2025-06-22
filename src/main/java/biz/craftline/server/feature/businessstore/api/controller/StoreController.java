package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/stores")
@RestController
public class StoreController {

    @Autowired
    StoreDTOMapper mapper;

    @Autowired
    private StoreService service;

    @Autowired
    private BusinessEntityService businessService;

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<StoreDTO>>> list() {
        List<Store> list = service.findAll();
        List<StoreDTO> dtoList = list.stream().map( mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    @GetMapping("/list/{businessId}")
    public ResponseEntity<APIResponse<List<StoreDTO>>> list(@PathVariable long businessId) {
        List<Store> list = service.findStoresByBusiness(businessId);
        List<StoreDTO> dtoList = list.stream().map( mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<StoreDTO>>> search(@RequestBody String keyword) {
        List<Store> list = service.findAll();
        List<StoreDTO> dtoList = list.stream().map( mapper::toDTO ).toList();
        return APIResponse.success(dtoList);
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<StoreDTO>> addStore(@RequestBody AddNewStoreRequest request) {

        Business business =null;
        if(request.getBusinessId()!=null){
            business = businessService.findById(request.getBusinessId()).get();
        }

        Store storeDomain = mapper.toDomain(request);
        storeDomain.setBusiness(business);

        Store savedStore = service.save(storeDomain);
        return APIResponse.success(mapper.toDTO(savedStore));
    }

}