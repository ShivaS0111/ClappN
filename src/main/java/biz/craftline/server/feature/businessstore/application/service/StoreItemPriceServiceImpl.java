package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class StoreItemPriceServiceImpl implements StoreItemPriceService {

    @Autowired
    StoreItemPriceEntityMapper mapper;

    @Autowired
    StoreItemPriceHandleRepository repository;


    @Override
    public StoreItemPrice save(StoreItemPrice entity) {
        return mapper.toDomain(repository.save(mapper.toEntity(entity)));
    }

    @Override
    public List<StoreItemPrice> findAllByProductLotId(Long productLotId) {
        return repository.findAllByProductLotId(productLotId)
                .parallelStream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StoreItemPrice> findAllByLotId(Long lotId) {
        return repository.findAllByProductLotId(lotId)
                .parallelStream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StoreItemPrice> findAllByProductId(Long productId) {
        // Use lotId for product price queries
        return repository.findAllByProductLotId(productId)
                .parallelStream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<StoreItemPrice> findByServiceId(Long serviceId) {
        Optional<StoreItemPriceEntity> storeItemPriceOptional = repository.findByServiceId(serviceId);
        return storeItemPriceOptional.map(mapper::toDomain);
    }

    @Override
    public Optional<StoreItemPrice> findByProductLotId(Long productLotId) {
        StoreItemPriceEntity storeItemPrice = repository.findByProductLotId(productLotId)
                .orElseThrow(() -> new RuntimeException("Product Lot Price not configured yet"));
        return Optional.of(mapper.toDomain(storeItemPrice));
    }

   /* @Override
    public Optional<StoreItemPrice> findByProductId(Long productId) {
        StoreItemPriceEntity storeItemPrice = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product Lot Price not configured yet"));
        return Optional.of(mapper.toDomain(storeItemPrice));
    }*/

    @Override
    public List<StoreItemPrice> findAllByServiceId(Long serviceId) {
        return repository.findAllByServiceId(serviceId)
                .parallelStream()
                .map(mapper::toDomain)
                .toList();
    }


    @Transactional
    @Override
    public Optional<StoreItemPrice> updateServicePrice(StoreItemPrice itemPrice) {
        Optional<StoreItemPriceEntity> storeItemPriceOptional = repository.findByServiceId(itemPrice.getServiceId());

        if(storeItemPriceOptional.isPresent()){
            StoreItemPriceEntity storeItemPrice = storeItemPriceOptional.get();
            // Set the validTo to current time to mark it as no longer valid
            storeItemPrice.setValidTo(LocalDateTime.now());
            repository.save(storeItemPrice);
        }

        StoreItemPriceEntity entity = repository.save( mapper.toEntity(itemPrice) );
        return Optional.of( mapper.toDomain(entity) );
    }

    @Transactional
    @Override
    public Optional<StoreItemPrice> updateProductPrice(StoreItemPrice itemPrice) {
        Optional<StoreItemPriceEntity> storeItemPriceOptional = repository.findByProductLotId(itemPrice.getProductLotId());

        if(storeItemPriceOptional.isPresent()){
            StoreItemPriceEntity storeItemPrice = storeItemPriceOptional.get();
            storeItemPrice.setValidTo(LocalDateTime.now());
            repository.save(storeItemPrice);
        }

        StoreItemPriceEntity entity = repository.save(mapper.toEntity(itemPrice));
        return Optional.of(mapper.toDomain(entity));
    }

    @Transactional
    @Override
    public Optional<StoreItemPrice> updateProductLotPrice(StoreItemPrice itemPrice) {
        Optional<StoreItemPriceEntity> storeItemPriceOptional = repository.findByProductLotId(itemPrice.getProductLotId());

        if(storeItemPriceOptional.isPresent()){
            StoreItemPriceEntity storeItemPrice = storeItemPriceOptional.get();
            storeItemPrice.setValidTo(LocalDateTime.now());
            repository.save(storeItemPrice);
        }

        StoreItemPriceEntity entity = repository.save(mapper.toEntity(itemPrice));
        return Optional.of(mapper.toDomain(entity));
    }

   /* @Override
    public Optional<StoreItemPrice> getConvertedPrice(Long id, String countryCode, String currency) {
        StoreItemPriceEntity entity =  repository.findByItemId(id)
                .orElseThrow( ()-> new RuntimeException("Item Price not configured yet"));
        return  Optional.of(mapper.toDomain(entity));
    }*/
}
