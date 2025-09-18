package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.service.BrandService;
import biz.craftline.server.feature.businesstype.infra.entity.BrandEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BrandEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BrandJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Override
    public List<Brand> findAll() {
        return brandJpaRepository.findAll()
                .stream()
                .map(BrandEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Brand findById(Long id) {
        return brandJpaRepository.findById(id)
                .map(BrandEntityMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Brand save(Brand brand) {
        BrandEntity entity = BrandEntityMapper.toEntity(brand);
        BrandEntity saved = brandJpaRepository.save(entity);
        return BrandEntityMapper.toDomain(saved);
    }

    @Override
    public void delete(Long id) {
        brandJpaRepository.deleteById(id);
    }
}

