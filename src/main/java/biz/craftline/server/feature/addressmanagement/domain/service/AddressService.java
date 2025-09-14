package biz.craftline.server.feature.addressmanagement.domain.service;

import biz.craftline.server.feature.addressmanagement.domain.model.Address;
import biz.craftline.server.feature.addressmanagement.api.mapper.AddressMapper;
import biz.craftline.server.feature.addressmanagement.infra.entity.AddressEntity;
import biz.craftline.server.feature.addressmanagement.infra.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAllAddresses() {
        return addressRepository.findAll().stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id).map(AddressMapper::toDomain);
    }

    public List<Address> getAddressesByTypeAndReferenceId(String type, Long referenceId) {
        return addressRepository.findByTypeAndReferenceId(type, referenceId).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Address createAddress(Address address) {
        AddressEntity entity = AddressMapper.toEntity(address);
        AddressEntity saved = addressRepository.save(entity);
        return AddressMapper.toDomain(saved);
    }

    public Address updateAddress(Long id, Address addressDetails) {
        return addressRepository.findById(id).map(entity -> {
            entity.setStreet(addressDetails.getStreet());
            entity.setCity(addressDetails.getCity());
            entity.setState(addressDetails.getState());
            entity.setPostalCode(addressDetails.getPostalCode());
            entity.setCountry(addressDetails.getCountry());
            entity.setType(addressDetails.getType());
            entity.setReferenceId(addressDetails.getReferenceId());
            AddressEntity updated = addressRepository.save(entity);
            return AddressMapper.toDomain(updated);
        }).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}

