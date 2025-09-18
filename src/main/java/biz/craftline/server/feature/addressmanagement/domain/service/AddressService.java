package biz.craftline.server.feature.addressmanagement.domain.service;

import biz.craftline.server.feature.addressmanagement.domain.model.Address;
import biz.craftline.server.feature.addressmanagement.api.mapper.AddressMapper;
import biz.craftline.server.feature.addressmanagement.infra.entity.AddressEntity;
import biz.craftline.server.feature.addressmanagement.infra.repository.AddressRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.AreaRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.CountryRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.DistrictRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.LandmarkRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.PlaceRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.RegionRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.SubRegionRepository;
import biz.craftline.server.feature.addressmanagement.infra.repository.ZipcodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class  AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private SubRegionRepository subRegionRepository;
    @Autowired
    private LandmarkRepository landmarkRepository;
    @Autowired
    private ZipcodeRepository zipcodeRepository;

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
        AddressEntity entity = new AddressEntity();
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setPostalCode(address.getPostalCode());
        entity.setType(address.getType());
        entity.setReferenceId(address.getReferenceId());
        entity.setDigiPin(address.getDigiPin());
        entity.setLatitude(address.getLatitude());
        entity.setLongitude(address.getLongitude());
        if (address.getAreaId() != null) entity.setArea(areaRepository.findById(address.getAreaId()).orElse(null));
        if (address.getPlaceId() != null) entity.setPlace(placeRepository.findById(address.getPlaceId()).orElse(null));
        if (address.getCountryId() != null) entity.setCountry(countryRepository.findById(address.getCountryId()).orElse(null));
        if (address.getDistrictId() != null) entity.setDistrict(districtRepository.findById(address.getDistrictId()).orElse(null));
        if (address.getRegionId() != null) entity.setRegion(regionRepository.findById(address.getRegionId()).orElse(null));
        if (address.getSubRegionId() != null) entity.setSubRegion(subRegionRepository.findById(address.getSubRegionId()).orElse(null));
        if (address.getLandmarkId() != null) entity.setLandmark(landmarkRepository.findById(address.getLandmarkId()).orElse(null));
        if (address.getZipcodeId() != null) entity.setZipcode(zipcodeRepository.findById(address.getZipcodeId()).orElse(null));
        AddressEntity saved = addressRepository.save(entity);
        return AddressMapper.toDomain(saved);
    }

    public Address updateAddress(Long id, Address addressDetails) {
        return addressRepository.findById(id).map(entity -> {
            entity.setStreet(addressDetails.getStreet());
            entity.setCity(addressDetails.getCity());
            entity.setState(addressDetails.getState());
            entity.setPostalCode(addressDetails.getPostalCode());
            //entity.setCountry(addressDetails.getCountry());
            entity.setType(addressDetails.getType());
            entity.setReferenceId(addressDetails.getReferenceId());
            AddressEntity updated = addressRepository.save(entity);
            return AddressMapper.toDomain(updated);
        }).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    public List<Address> getAddressesByArea(String area) {
        return addressRepository.findByArea_Name(area).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesByPlace(String place) {
        return addressRepository.findByPlace_Name(place).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesByDistrict(String district) {
        return addressRepository.findByDistrict_Name(district).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesByRegion(String region) {
        return addressRepository.findByRegion_Name(region).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesBySubRegion(String subRegion) {
        return addressRepository.findBySubRegion_Name(subRegion).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesByLandmark(String landmark) {
        return addressRepository.findByLandmark_Name(landmark).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
    public List<Address> getAddressesByZipcode(String zipcode) {
        return addressRepository.findByZipcode_Code(zipcode).stream()
            .map(AddressMapper::toDomain)
            .collect(Collectors.toList());
    }
}
