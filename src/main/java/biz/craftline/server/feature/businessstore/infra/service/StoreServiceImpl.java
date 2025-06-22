package biz.craftline.server.feature.businessstore.infra.service;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    StoreEntityMapper storeEntityMapper;

    @Autowired
    StoreRepository storeRepository;


    @Override
    public List<Store> findAll() {
        return storeRepository.findAll().stream().map(storeEntityMapper::toDomain).toList();
    }

    @Override
    public List<Store> searchStores(String keyword) {
        return storeRepository.searchStoreByStoreName(keyword).stream().map(storeEntityMapper::toDomain).toList();
    }

    @Override
    public void deleteBusinessTypeById(Long id) {
        storeRepository.deleteBusinessTypeById(id);
    }

    @Override
    public Optional<Store> findById(Long id) {
        Optional<StoreEntity> storeEntity =  storeRepository.findById(id);
        return Optional.of( storeEntityMapper.toDomain(storeEntity.orElseThrow()));
    }

    @Override
    public Store save(Store entity) {
        StoreEntity storeEntity = storeRepository.save(storeEntityMapper.toEntity(entity));
        return storeEntityMapper.toDomain(storeEntity);
    }
}