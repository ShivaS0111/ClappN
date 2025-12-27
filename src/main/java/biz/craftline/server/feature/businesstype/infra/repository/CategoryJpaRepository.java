package biz.craftline.server.feature.businesstype.infra.repository;

import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findByParentIsNull();

    @Query("SELECT c FROM CategoryEntity  c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<CategoryEntity> searchByKeyword(@Param("keyword") String keyword);

    List<CategoryEntity> findAllByIdIn(Set<Long> ids);
}
