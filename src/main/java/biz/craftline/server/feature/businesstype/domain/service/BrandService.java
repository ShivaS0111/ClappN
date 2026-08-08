package biz.craftline.server.feature.businesstype.domain.service;

import biz.craftline.server.feature.businesstype.domain.model.Brand;
import java.util.List;

public interface BrandService {
    List<Brand> findAll();
    Brand findById(Long id);
    Brand save(Brand brand);

    Brand update(Long id, Brand brand);

    void delete(Long id);
}

