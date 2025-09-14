package biz.craftline.server.feature.employeemanagement.infra.repository;

import biz.craftline.server.feature.employeemanagement.infra.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    List<EmployeeEntity> findByStoreId(String storeId);
    List<EmployeeEntity> findByBusinessId(String businessId);
}

