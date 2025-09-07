package biz.craftline.server.feature.businesstype.infra.repository;

import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIsNull();
}
