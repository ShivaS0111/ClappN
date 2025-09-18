package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByTypeAndReferenceId(String type, Long referenceId);
    List<AddressEntity> findByArea_Name(String areaName);
    List<AddressEntity> findByPlace_Name(String placeName);
    List<AddressEntity> findByDistrict_Name(String districtName);
    List<AddressEntity> findByRegion_Name(String regionName);
    List<AddressEntity> findBySubRegion_Name(String subRegionName);
    List<AddressEntity> findByLandmark_Name(String landmarkName);
    List<AddressEntity> findByZipcode_Code(String code);
}
