package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessProductDTOMapper;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchServiceByBusinessRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.util.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/business-product")
public class BusinessProductController {

    @Autowired
    private BusinessProductDTOMapper mapper;

    @Autowired
    private BusinessProductsService service;

    @Autowired
    private BusinessTypeService businessTypeService;


    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> list() {
        List<BusinessProduct> list = service.findAll();
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> search(
            @RequestBody SearchServiceByBusinessRequest search) {
        List<BusinessProduct> list = service.findBySearch(search.keyword());
        System.out.println("===>Keyword: " + search.keyword() + ", resp:  " + list);
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search-service-by-business")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> searchServiceByBusiness(
            @RequestBody SearchServiceByBusinessRequest request) {
        List<BusinessProduct> list = service.findByBusinessIdAndSearch(request.business_id(), request.keyword());
        return APIResponse.success(convertToDTOList(list));
    }

    @GetMapping("/business-type/{id}")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> searchByBusiness(
            @PathVariable("id") long businessType) {
        List<BusinessProduct> list = service.findAllByBusinessTypeId(businessType);
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessProductDTO>> add(@RequestBody BusinessProductDTO dto) {
        BusinessProduct bs = service.save(mapper.toDomain(dto));
        return APIResponse.success(mapper.toDTO(bs));
    }

    @PostMapping("/add-all2")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> addAll2(@RequestBody List<AddNewBusinessProductRequest> request) {

        List<Long> businessTypeIds = request.stream().map( AddNewBusinessProductRequest::getBusinessType).toList();
        Map<Long, BusinessType> businessTypeMap = businessTypeService.findAllByIds(businessTypeIds)
                .stream()
                .collect(Collectors.toMap(BusinessType::getId, Function.identity()));

        List<BusinessProductDTO> list = new ArrayList<>();
        for (AddNewBusinessProductRequest dto: request){
            try {
                BusinessProduct bs = mapper.toDomain(dto);
                bs.setBusinessType(businessTypeMap.get( dto.getBusinessType()));
                list.add(mapper.toDTO(service.save(bs)));
            }catch (Exception e){ e.printStackTrace();}
        }
        return APIResponse.success(list);
    }

    @PostMapping("/add-all")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> addAll(
            @RequestBody List<AddNewBusinessProductRequest> requests) {

        Map<Long, BusinessType> businessTypeMap = businessTypeService
                .findAllByIds(requests.stream().map(AddNewBusinessProductRequest::getBusinessType).toList())
                .stream()
                .collect(Collectors.toMap(BusinessType::getId, Function.identity()));

        List<BusinessProduct> servicesList = requests.stream()
                .map(request -> {
                    try {
                        BusinessProduct domain = mapper.toDomain(request);
                        domain.setBusinessType(businessTypeMap.get(request.getBusinessType()));
                        return domain;
                    } catch (Exception e) {
                        e.printStackTrace(); // You may want to log this properly instead
                        return null; // Or handle errors differently
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        List<BusinessProduct> result = service.save(servicesList);
        return APIResponse.success( result!=null?result.parallelStream().map(mapper::toDTO).toList(): new ArrayList<>());
    }

    @PostMapping("/add-all1")
    public ResponseEntity<APIResponse<List<BusinessProductDTO>>> add(@RequestBody List<BusinessProductDTO> list) {
        List<BusinessProduct> services = list.stream().map(item -> mapper.toDomain(item)).toList();
        return APIResponse.success(convertToDTOList(service.save(services)));
    }

    private List<BusinessProductDTO> convertToDTOList(List<BusinessProduct> list) {
        return list.parallelStream().map(mapper::toDTO).toList();
    }
}

