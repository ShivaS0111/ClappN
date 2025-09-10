package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;

import java.util.List;
import java.util.Optional;

public interface BusinessProductsService {

    List<BusinessProduct> findAll();

    void deleteServiceById(Long id);

    Optional<BusinessProduct> findById(Long businessTypeId);

    BusinessProduct save(BusinessProduct product);

    List<BusinessProduct> save(List<BusinessProduct> product);

    List<BusinessProduct> findByBusinessIdAndSearch(Long id, String keyword);

    List<BusinessProduct> findBySearch(String keyword);

}