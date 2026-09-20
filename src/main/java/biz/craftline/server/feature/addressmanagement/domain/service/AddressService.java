package biz.craftline.server.feature.addressmanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
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
import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import biz.craftline.server.feature.customermanagement.infra.repository.CustomerRepository;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService {
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
    @Autowired
    private SecurityContextService securityContextService;
    @Autowired
    private UserService userService;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public List<Address> getAllAddresses() {
        return filterAccessible(addressRepository.findAll().stream()
                .map(AddressMapper::toDomain)
                .collect(Collectors.toList()));
    }

    public Optional<Address> getAddressById(Long id) {
        Optional<Address> address = addressRepository.findById(id).map(AddressMapper::toDomain);
        address.ifPresent(this::assertCanAccessAddress);
        return address;
    }

    public List<Address> getAddressesByTypeAndReferenceId(String type, Long referenceId) {
        assertCanAccessTypeAndReference(type, referenceId);
        return addressRepository.findByTypeAndReferenceId(type, referenceId).stream()
                .map(AddressMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Address createAddress(Address address) {
        assertCanAccessTypeAndReference(address.getType(), address.getReferenceId());
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
            assertCanAccessAddress(AddressMapper.toDomain(entity));
            if (addressDetails.getType() != null && addressDetails.getReferenceId() != null) {
                assertCanAccessTypeAndReference(addressDetails.getType(), addressDetails.getReferenceId());
            }
            entity.setStreet(addressDetails.getStreet());
            entity.setCity(addressDetails.getCity());
            entity.setState(addressDetails.getState());
            entity.setPostalCode(addressDetails.getPostalCode());
            entity.setType(addressDetails.getType());
            entity.setReferenceId(addressDetails.getReferenceId());
            AddressEntity updated = addressRepository.save(entity);
            return AddressMapper.toDomain(updated);
        }).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    public void deleteAddress(Long id) {
        addressRepository.findById(id).ifPresent(entity -> {
            assertCanAccessAddress(AddressMapper.toDomain(entity));
            addressRepository.deleteById(id);
        });
    }

    public List<Address> getAddressesByArea(String area) {
        return filterAccessible(addressRepository.findByArea_Name(area).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesByPlace(String place) {
        return filterAccessible(addressRepository.findByPlace_Name(place).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesByDistrict(String district) {
        return filterAccessible(addressRepository.findByDistrict_Name(district).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesByRegion(String region) {
        return filterAccessible(addressRepository.findByRegion_Name(region).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesBySubRegion(String subRegion) {
        return filterAccessible(addressRepository.findBySubRegion_Name(subRegion).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesByLandmark(String landmark) {
        return filterAccessible(addressRepository.findByLandmark_Name(landmark).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    public List<Address> getAddressesByZipcode(String zipcode) {
        return filterAccessible(addressRepository.findByZipcode_Code(zipcode).stream()
                .map(AddressMapper::toDomain).collect(Collectors.toList()));
    }

    private List<Address> filterAccessible(List<Address> addresses) {
        if (securityContextService.isSystemAdmin()) {
            return addresses;
        }
        return addresses.stream().filter(this::canAccessAddress).collect(Collectors.toList());
    }

    private void assertCanAccessAddress(Address address) {
        if (!canAccessAddress(address)) {
            throw new AccessDeniedException("You do not have access to this address");
        }
    }

    private boolean canAccessAddress(Address address) {
        if (address == null) {
            return false;
        }
        if (securityContextService.isSystemAdmin()) {
            return true;
        }
        try {
            assertCanAccessTypeAndReference(address.getType(), address.getReferenceId());
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

    /**
     * Scope address ownership via type + referenceId:
     * BUSINESS → business access; STORE → store access;
     * USER → same rules as user visibility; EMPLOYEE → employee store/business;
     * CUSTOMER → customer store/business; DELIVERY → admin only (no reliable store link).
     */
    private void assertCanAccessTypeAndReference(String type, Long referenceId) {
        if (type == null || referenceId == null) {
            throw new AccessDeniedException("Address type and referenceId are required");
        }
        if (securityContextService.isSystemAdmin()) {
            return;
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "BUSINESS" -> securityContextService.validateBusinessAccess(referenceId);
            case "STORE" -> securityContextService.validateStoreAccess(referenceId);
            case "USER" -> {
                // getUserById enforces store/business overlap (or self / admin)
                userService.getUserById(referenceId)
                        .orElseThrow(() -> new AccessDeniedException("User not found for address"));
            }
            case "EMPLOYEE" -> {
                MembershipEntity membership = membershipRepository.findById(referenceId)
                        .orElseThrow(() -> new AccessDeniedException("Employee/membership not found for address"));
                if (membership.getStoreScopes() != null && !membership.getStoreScopes().isEmpty()) {
                    membership.getStoreScopes().forEach(securityContextService::validateStoreAccess);
                } else if (membership.getBusinessId() != null) {
                    securityContextService.validateBusinessAccess(membership.getBusinessId());
                } else {
                    throw new AccessDeniedException("Membership has no store/business scope");
                }
            }
            case "CUSTOMER" -> {
                CustomerEntity customer = customerRepository.findById(referenceId)
                        .orElseThrow(() -> new AccessDeniedException("Customer not found for address"));
                if (customer.getStoreId() != null) {
                    securityContextService.validateStoreAccess(customer.getStoreId());
                } else if (customer.getBusinessId() != null) {
                    securityContextService.validateBusinessAccess(customer.getBusinessId());
                } else {
                    throw new AccessDeniedException("Customer has no store/business scope");
                }
            }
            case "DELIVERY" -> throw new AccessDeniedException("Delivery address access requires SYSTEM_ADMIN or order APIs");
            default -> throw new AccessDeniedException("Unknown address type: " + type);
        }
    }
}
