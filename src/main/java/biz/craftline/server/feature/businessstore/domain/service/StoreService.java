package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StoreService {

    List<Store> findAll();

    List<Store> searchStores(String keyword);

    void deleteBusinessTypeById(Long id);

    Optional<Store> findById(Long id);

    Store save(Store entity);
}