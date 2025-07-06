package biz.craftline.server.feature.businessstore.infra.service;

import biz.craftline.server.feature.businessstore.api.dto.StoreProductPriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreProductPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreProductPriceService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreProductPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreProductPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.StoreServicePriceHandleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class StoreServicePriceHandleServiceImpl implements StoreProductPriceService {

    @Autowired
    StoreProductPriceEntityMapper mapper;

    @Autowired
    StoreServicePriceHandleRepository repository;


    @Override
    public StoreProductPrice save(StoreProductPrice entity) {
         return mapper.toDomain( repository.save(mapper.toEntity(entity)) );
    }

    @Override
    public List<StoreProductPrice> findAllByProductIdAndProductType(Long productId, Long productType){
        return repository.findAllByProductIdAndProductType(productId, productType).parallelStream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<StoreProductPrice> getConvertedPrice(Long id, String countryCode, String currency) {
        StoreProductPriceEntity entity =  repository.findCurrentPrice(id, countryCode).orElseThrow();
        return  Optional.of(mapper.toDomain(entity));
    }
}
