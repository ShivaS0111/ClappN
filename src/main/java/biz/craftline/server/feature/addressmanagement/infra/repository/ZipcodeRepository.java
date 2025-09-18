package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.ZipcodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZipcodeRepository extends JpaRepository<ZipcodeEntity, Long> {
    boolean existsByCode(String code);
    ZipcodeEntity findByCode(String code);
}

