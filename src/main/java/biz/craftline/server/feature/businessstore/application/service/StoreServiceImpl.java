package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
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

    @Autowired
    BusinessEntityJpaRepository businessRepository;

    @Autowired
    UserService userService;


    @Override
    public List<Store> findAll() {
        return storeRepository.findAll().stream().map(storeEntityMapper::toDomain).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Store> searchStores(String keyword) {
        return storeRepository.searchStoreByStoreName(keyword).stream().map(storeEntityMapper::toDomain).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Store> findStoresByBusiness(long business) {
        return storeRepository.findByBusinessId(business)
                .stream()
                .map(storeEntityMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Store findStoreById(long storeId) {
         StoreEntity storeEntity = storeRepository.findById(storeId)
                .orElseThrow(()->new RuntimeException("Store not found: "+storeId));
         return storeEntityMapper.toDomain(storeEntity);
    }

    @Override
    public void deleteStoreById(Long id) {
        storeRepository.deleteStoreById(id);
    }

    @Override
    public Optional<Store> findById(Long id) {
        return storeRepository.findById(id).map(storeEntityMapper::toDomain);
    }

    @Override
    public Store save(Store domain) {
        String currentUsername = UserUtil.requireCurrentUsername();
        Long loggedUserId = userService.getUserByEmail(currentUsername)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        StoreEntity entity = storeEntityMapper.toEntity(domain);
        if (domain.getBusiness() != null) {
            Optional<BusinessEntity> businessEntity = businessRepository.findById(domain.getBusiness().getId());
            if (businessEntity.isPresent()) {
                entity.setBusiness(businessEntity.get());
            }
        }
        entity.setCreatedBy(loggedUserId);
        StoreEntity storeEntity = storeRepository.save(entity);
        return storeEntityMapper.toDomain(storeEntity);
    }
}