package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchServiceByBusinessRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.util.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/business-service")
public class BusinessServiceController {

    @Autowired
    private BusinessServiceDTOMapper mapper;

    @Autowired
    private BusinessServicesService service;

    @Autowired
    private BusinessTypeService businessTypeService;


    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> list() {
        List<BusinessService> list = service.findAll();
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> search(
            @RequestBody SearchRequest search) {
        List<BusinessService> list = service.findBySearch(search.keyword());
        System.out.println("===>Keyword: " + search.keyword() + ", resp:  " + list);
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/search-service-by-business")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> searchServiceByBusiness(
            @RequestBody SearchServiceByBusinessRequest request) {
        List<BusinessService> list = service.findByBusinessIdAndSearch(request.business_id(), request.keyword());
        return APIResponse.success(convertToDTOList(list));
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<BusinessServiceDTO>> add(@RequestBody BusinessServiceDTO dto) {
        BusinessService bs = service.save(mapper.toDomain(dto));
        return APIResponse.success(mapper.toDTO(bs));
    }

    @PostMapping("/add-all2")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> addAll2(@RequestBody List<AddNewBusinessServiceRequest> request) {

        List<Long> businessTypeIds = request.stream().map( AddNewBusinessServiceRequest::getBusinessType).toList();
        Map<Long, BusinessType> businessTypeMap = businessTypeService.findAllByIds(businessTypeIds)
                .stream()
                .collect(Collectors.toMap(BusinessType::getId, Function.identity()));

        List<BusinessServiceDTO> list = new ArrayList<>();
        for (AddNewBusinessServiceRequest dto: request){
            try {
                BusinessService bs = mapper.toDomain(dto);
                bs.setBusinessType(businessTypeMap.get( dto.getBusinessType()));
                list.add(mapper.toDTO(service.save(bs)));
            }catch (Exception e){ e.printStackTrace();}
        }
        return APIResponse.success(list);
    }

    @PostMapping("/add-all")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> addAll(
            @RequestBody List<AddNewBusinessServiceRequest> requests) {

        Map<Long, BusinessType> businessTypeMap = businessTypeService
                .findAllByIds(requests.stream().map(AddNewBusinessServiceRequest::getBusinessType).toList())
                .stream()
                .collect(Collectors.toMap(BusinessType::getId, Function.identity()));

        List<BusinessService> servicesList = requests.stream()
                .map(request -> {
                    try {
                        BusinessService domain = mapper.toDomain(request);
                        domain.setBusinessType(businessTypeMap.get(request.getBusinessType()));
                        return domain;
                    } catch (Exception e) {
                        e.printStackTrace(); // You may want to log this properly instead
                        return null; // Or handle errors differently
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        List<BusinessService> result = service.save(servicesList);
        return APIResponse.success( result!=null?result.parallelStream().map(mapper::toDTO).toList(): new ArrayList<>());
    }

    @PostMapping("/add-all1")
    public ResponseEntity<APIResponse<List<BusinessServiceDTO>>> add(@RequestBody List<BusinessServiceDTO> list) {
        List<BusinessService> services = list.stream().map(item -> mapper.toDomain(item)).toList();
        return APIResponse.success(convertToDTOList(service.save(services)));
    }

    private List<BusinessServiceDTO> convertToDTOList(List<BusinessService> list) {
        return list.parallelStream().map(mapper::toDTO).toList();
    }
}

