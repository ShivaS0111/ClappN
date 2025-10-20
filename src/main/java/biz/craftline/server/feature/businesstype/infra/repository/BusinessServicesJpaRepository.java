package biz.craftline.server.feature.businesstype.infra.repository;

import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessServicesJpaRepository extends JpaRepository<BusinessServiceEntity, Long> {

    // Search services by keyword in serviceName or description
    /*@Query("SELECT bs FROM business_service bs WHERE " +
            "(LOWER(bs.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BusinessServiceEntity> searchByKeyword(@Param("keyword") String keyword);


    @Query("SELECT bs FROM business_service bs WHERE bs.businessType = :businessTypeId " +
            "AND (LOWER(bs.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BusinessServiceEntity> searchByKeywordAndBusinessTypeId(@Param("keyword") String keyword,
                                                           @Param("businessTypeId") Long businessTypeId);

    @Query("SELECT bs FROM business_service bs WHERE bs.businessType = :businessTypeId ")
    List<BusinessServiceEntity> findAllByBusinessTypeId(@Param("businessTypeId") Long businessTypeId);

    @Query("SELECT bs FROM business_service bs WHERE bs.businessType = :businessTypeId " +
            "AND (LOWER(bs.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BusinessServiceEntity> searchByKeywordAndBusinessTypeIdPageable(@Param("keyword") String keyword,
                                                                   @Param("businessTypeId") Long businessTypeId,
                                                                   Pageable pageable);*/

    @Query("SELECT bs FROM business_service bs WHERE bs.businessType.id = :businessTypeId " +
            "AND (LOWER(bs.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BusinessServiceEntity> searchByKeywordAndBusinessType(@Param("keyword") String keyword,
                                                               @Param("businessTypeId") Long businessTypeId);

    List<BusinessServiceEntity> save(List<BusinessServiceEntity> list);

    // Method 1: Using the business type's ID
    List<BusinessServiceEntity> findByBusinessType_Id(Long businessTypeId);

    // (Optional) Method 2: Using the BusinessTypeEntity directly
    List<BusinessServiceEntity> findByBusinessType(BusinessTypeEntity businessType);

}
