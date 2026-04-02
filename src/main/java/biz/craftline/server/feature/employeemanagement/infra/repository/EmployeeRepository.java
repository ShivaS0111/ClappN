package biz.craftline.server.feature.employeemanagement.infra.repository;

import biz.craftline.server.feature.employeemanagement.infra.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    List<EmployeeEntity> findByStoreId(Long storeId);
    List<EmployeeEntity> findByBusinessId(Long businessId);
    List<EmployeeEntity> findByUserId(Long userId);

    List<EmployeeEntity> findByStoreIdIn(List<Long> storeIds);

    List<EmployeeEntity> findByBusinessIdIn(List<Long> businessIds);
}
