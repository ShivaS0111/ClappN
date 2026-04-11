package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessEntityService {

    List<Business> findAll();

    void deleteBusinessTypeById(Long id);

    Optional<Business> findById(Long id);

    Business save(Business business);

    List<Business> search(String keyword);

    /**
     * Creates a new business and its owner (user, role assignment, employee record) in a single transaction.
     */
    Business createBusinessWithOwner(Business business, String ownerName, String ownerEmail, String ownerPhone, String ownerPassword);
}