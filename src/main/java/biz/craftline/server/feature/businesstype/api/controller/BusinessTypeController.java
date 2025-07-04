package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessTypeDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessTypeRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/business-type")
public class BusinessTypeController {

    @Autowired
    private BusinessTypeService service;

    @Autowired
    private BusinessServicesService businessService;

    @Autowired
    private BusinessTypeDTOMapper mapper;

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessTypeDTO>>> list() {
        List<BusinessType> list = service.findAll();
        return APIResponse.success(list.stream().map(mapper::toDTO).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessTypeDTO>>> search(@RequestBody String keyword) {
        List<BusinessType> list = service.findAll();
        return APIResponse.success(list.stream().map(mapper::toDTO).toList());
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessTypeDTO>> add(@RequestBody AddNewBusinessTypeRequest request) {
        System.out.println("===>BusinessType: " + request + ", name:"
                + request.getBusinessName() + ", desc:" + request.getDescription());
        BusinessTypeDTO dto =  BusinessTypeDTO.builder()
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .build();
        BusinessType businessType = service.save(mapper.toDomain(dto));
        return APIResponse.success(mapper.toDTO(businessType));
    }

    @PostMapping("/add-all")
    public ResponseEntity<APIResponse<List<BusinessTypeDTO>>> addAll(@RequestBody List<AddNewBusinessTypeRequest> requests) {
        var list =requests.stream().map( request-> {
            System.out.println("===>BusinessType: " + request + ", name:" + request.getBusinessName()
                    + ", desc:" + request.getDescription());
            BusinessTypeDTO dto =  BusinessTypeDTO.builder()
                    .businessName(request.getBusinessName())
                    .description(request.getDescription())
                    .build();
            return mapper.toDTO(service.save(mapper.toDomain(dto)));
        }).toList();
        return APIResponse.success( list );
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<APIResponse<BusinessTypeDTO>> details(@PathVariable("id") String id) {
        BusinessType businessType = service.findById(Long.valueOf(id)).orElseThrow();
        List<BusinessService> services =  businessService.findAllByBusinessTypeId(businessType.getId());
        businessType.setServices(services);
        return APIResponse.success(mapper.toDTO(businessType));
    }

}
