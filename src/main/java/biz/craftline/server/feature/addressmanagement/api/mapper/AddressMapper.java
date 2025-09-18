package biz.craftline.server.feature.addressmanagement.api.mapper;

import biz.craftline.server.feature.addressmanagement.api.dto.AddressDto;
import biz.craftline.server.feature.addressmanagement.api.request.AddressRequest;
import biz.craftline.server.feature.addressmanagement.api.response.AddressResponse;
import biz.craftline.server.feature.addressmanagement.domain.model.Address;
import biz.craftline.server.feature.addressmanagement.infra.entity.AddressEntity;

public class AddressMapper {
    public static AddressDto toDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setType(address.getType());
        dto.setReferenceId(address.getReferenceId());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        dto.setDigiPin(address.getDigiPin());
        dto.setAreaId(address.getAreaId());
        dto.setAreaName(address.getAreaName());
        dto.setPlaceId(address.getPlaceId());
        dto.setPlaceName(address.getPlaceName());
        dto.setCountryId(address.getCountryId());
        dto.setCountryName(address.getCountryName());
        dto.setDistrictId(address.getDistrictId());
        dto.setDistrictName(address.getDistrictName());
        dto.setRegionId(address.getRegionId());
        dto.setRegionName(address.getRegionName());
        dto.setSubRegionId(address.getSubRegionId());
        dto.setSubRegionName(address.getSubRegionName());
        dto.setLandmarkId(address.getLandmarkId());
        dto.setLandmarkName(address.getLandmarkName());
        dto.setZipcodeId(address.getZipcodeId());
        dto.setZipcodeName(address.getZipcodeName());
        return dto;
    }
    public static AddressResponse toResponse(Address address) {
        AddressResponse resp = new AddressResponse();
        resp.setId(address.getId());
        resp.setStreet(address.getStreet());
        resp.setCity(address.getCity());
        resp.setState(address.getState());
        resp.setPostalCode(address.getPostalCode());
        resp.setCountry(address.getCountry());
        resp.setType(address.getType());
        resp.setReferenceId(address.getReferenceId());
        resp.setLatitude(address.getLatitude());
        resp.setLongitude(address.getLongitude());
        resp.setDigiPin(address.getDigiPin());
        resp.setAreaId(address.getAreaId());
        resp.setAreaName(address.getAreaName());
        resp.setPlaceId(address.getPlaceId());
        resp.setPlaceName(address.getPlaceName());
        resp.setCountryId(address.getCountryId());
        resp.setCountryName(address.getCountryName());
        resp.setDistrictId(address.getDistrictId());
        resp.setDistrictName(address.getDistrictName());
        resp.setRegionId(address.getRegionId());
        resp.setRegionName(address.getRegionName());
        resp.setSubRegionId(address.getSubRegionId());
        resp.setSubRegionName(address.getSubRegionName());
        resp.setLandmarkId(address.getLandmarkId());
        resp.setLandmarkName(address.getLandmarkName());
        resp.setZipcodeId(address.getZipcodeId());
        resp.setZipcodeName(address.getZipcodeName());
        return resp;
    }
    public static Address toDomain(AddressEntity entity) {
        Address address = new Address();
        address.setId(entity.getId());
        address.setStreet(entity.getStreet());
        address.setCity(entity.getCity());
        address.setState(entity.getState());
        address.setPostalCode(entity.getPostalCode());
        address.setCountryId(entity.getCountry() != null ? entity.getCountry().getId() : null);
        address.setCountryName(entity.getCountry() != null ? entity.getCountry().getName() : null);
        address.setType(entity.getType());
        address.setReferenceId(entity.getReferenceId());
        address.setLatitude(entity.getLatitude());
        address.setLongitude(entity.getLongitude());
        address.setDigiPin(entity.getDigiPin());
        address.setAreaId(entity.getArea() != null ? entity.getArea().getId() : null);
        address.setAreaName(entity.getArea() != null ? entity.getArea().getName() : null);
        address.setPlaceId(entity.getPlace() != null ? entity.getPlace().getId() : null);
        address.setPlaceName(entity.getPlace() != null ? entity.getPlace().getName() : null);
        address.setDistrictId(entity.getDistrict() != null ? entity.getDistrict().getId() : null);
        address.setDistrictName(entity.getDistrict() != null ? entity.getDistrict().getName() : null);
        address.setRegionId(entity.getRegion() != null ? entity.getRegion().getId() : null);
        address.setRegionName(entity.getRegion() != null ? entity.getRegion().getName() : null);
        address.setSubRegionId(entity.getSubRegion() != null ? entity.getSubRegion().getId() : null);
        address.setSubRegionName(entity.getSubRegion() != null ? entity.getSubRegion().getName() : null);
        address.setLandmarkId(entity.getLandmark() != null ? entity.getLandmark().getId() : null);
        address.setLandmarkName(entity.getLandmark() != null ? entity.getLandmark().getName() : null);
        address.setZipcodeId(entity.getZipcode() != null ? entity.getZipcode().getId() : null);
        address.setZipcodeName(entity.getZipcode() != null ? entity.getZipcode().getCode() : null);
        return address;
    }
    public static AddressEntity toEntity(Address address) {
        AddressEntity entity = new AddressEntity();
        entity.setId(address.getId());
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setPostalCode(address.getPostalCode());
        entity.setType(address.getType());
        entity.setReferenceId(address.getReferenceId());

        entity.setLatitude(address.getLatitude());
        entity.setLongitude(address.getLongitude());
        entity.setDigiPin(address.getDigiPin());
        // Removed mapping for area, place, district, region, subRegion, landmark, zipcode objects
        // Only set entity references in service layer, not here
        return entity;
    }
    public static Address toDomain(AddressRequest req) {
        Address address = new Address();
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPostalCode(req.getPostalCode());
        address.setCountry(req.getCountry());
        address.setType(req.getType());
        address.setReferenceId(req.getReferenceId());

        address.setLatitude(req.getLatitude());
        address.setLongitude(req.getLongitude());
        address.setDigiPin(req.getDigiPin());
        address.setAreaId(req.getAreaId());
        address.setPlaceId(req.getPlaceId());
        address.setCountryId(req.getCountryId());
        address.setDistrictId(req.getDistrictId());
        address.setRegionId(req.getRegionId());
        address.setSubRegionId(req.getSubRegionId());
        address.setLandmarkId(req.getLandmarkId());
        address.setZipcodeId(req.getZipcodeId());
        return address;
    }
}
