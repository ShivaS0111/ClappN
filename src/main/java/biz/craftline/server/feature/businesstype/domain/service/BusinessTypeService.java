package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface BusinessTypeService {

    List<BusinessType> findAll();
    List<BusinessType> findByNameContaining(String keyword);

    void deleteBusinessTypeById(Long id);

    Optional<BusinessType> findById(Long id);

    BusinessType save(BusinessType businessType);

    List<BusinessType>  findAllByIds(List<Long> businessTypeIds);
}

