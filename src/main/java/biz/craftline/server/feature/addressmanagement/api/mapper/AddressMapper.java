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
        return resp;
    }
    public static Address toDomain(AddressEntity entity) {
        Address address = new Address();
        address.setId(entity.getId());
        address.setStreet(entity.getStreet());
        address.setCity(entity.getCity());
        address.setState(entity.getState());
        address.setPostalCode(entity.getPostalCode());
        address.setCountry(entity.getCountry());
        address.setType(entity.getType());
        address.setReferenceId(entity.getReferenceId());

        address.setLatitude(entity.getLatitude());
        address.setLongitude(entity.getLongitude());
        address.setDigiPin(entity.getDigiPin());
        return address;
    }
    public static AddressEntity toEntity(Address address) {
        AddressEntity entity = new AddressEntity();
        entity.setId(address.getId());
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setPostalCode(address.getPostalCode());
        entity.setCountry(address.getCountry());
        entity.setType(address.getType());
        entity.setReferenceId(address.getReferenceId());

        entity.setLatitude(address.getLatitude());
        entity.setLongitude(address.getLongitude());
        entity.setDigiPin(address.getDigiPin());
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
        return address;
    }
}

