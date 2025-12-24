package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;

import java.util.List;
import java.util.Optional;

public interface BusinessServicesService {

    List<BusinessService> findAll();

    void deleteServiceById(Long id);

    Optional<BusinessService> findById(Long businessTypeId);

    BusinessService save(BusinessService businessService);

    List<BusinessService> save(List<BusinessService> businessService);

    List<BusinessService> searchByKeywordAndBusinessType(Long businessTypeId, String keyword);

    List<BusinessService> findBySearch(String keyword);

    List<BusinessService> findByBusinessTypeId(Long businessTypeId);

    BusinessService update(BusinessService domain);
}