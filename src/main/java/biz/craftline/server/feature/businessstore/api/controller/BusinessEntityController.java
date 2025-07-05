package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.BusinessDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/business")
@RestController
public class BusinessEntityController {

    @Autowired
    BusinessDTOMapper mapper;

    @Autowired
    private BusinessEntityService service;


    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessDTO>>> list() {
        List<Business> list = service.findAll();

        List<BusinessDTO> dtoList = list.stream().map( mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessDTO>>> search(@RequestBody String keyword) {
        List<Business> list = service.search(keyword);
        List<BusinessDTO> dtoList = list.stream().map( mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessDTO>> addBusiness(@RequestBody AddNewBusinessRequest request) {

        Business business = mapper.toDomain(request);
        Business savedBusiness = service.save(business);
        return APIResponse.success(mapper.toDTO(savedBusiness));
    }

}
