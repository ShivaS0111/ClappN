package biz.craftline.server.feature.addressmanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.addressmanagement.domain.model.Address;
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
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private AreaRepository areaRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private SubRegionRepository subRegionRepository;
    @Mock private LandmarkRepository landmarkRepository;
    @Mock private ZipcodeRepository zipcodeRepository;
    @Mock private SecurityContextService securityContextService;
    @Mock private UserService userService;
    @Mock private MembershipRepository membershipRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContextService.isSystemAdmin()).thenReturn(false);
        lenient().doNothing().when(securityContextService).validateStoreAccess(anyLong());
        lenient().doNothing().when(securityContextService).validateBusinessAccess(anyLong());
    }

    private AddressEntity entity(Long id, String type, Long refId) {
        AddressEntity e = new AddressEntity();
        e.setId(id);
        e.setStreet("Main");
        e.setCity("City");
        e.setState("ST");
        e.setPostalCode("10001");
        e.setType(type);
        e.setReferenceId(refId);
        return e;
    }

    private Address domain(String type, Long refId) {
        Address a = new Address();
        a.setStreet("Main");
        a.setCity("City");
        a.setState("ST");
        a.setPostalCode("10001");
        a.setType(type);
        a.setReferenceId(refId);
        return a;
    }

    @Test
    void getAllAddresses_asAdmin_returnsAll() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findAll()).thenReturn(List.of(entity(1L, "STORE", 10L)));
        List<Address> result = addressService.getAllAddresses();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAddressById_present() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "STORE", 10L)));
        Optional<Address> result = addressService.getAddressById(1L);
        assertTrue(result.isPresent());
        assertEquals("STORE", result.get().getType());
    }

    @Test
    void createAddress_storeType_saves() {
        Address input = domain("STORE", 5L);
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(inv -> {
            AddressEntity saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Address created = addressService.createAddress(input);
        assertEquals(99L, created.getId());
        assertEquals("STORE", created.getType());
        verify(securityContextService).validateStoreAccess(5L);
        verify(addressRepository).save(any(AddressEntity.class));
    }

    @Test
    void createAddress_businessType_validatesBusiness() {
        Address input = domain("BUSINESS", 7L);
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(inv -> {
            AddressEntity saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        addressService.createAddress(input);
        verify(securityContextService).validateBusinessAccess(7L);
    }

    @Test
    void updateAddress_notFound_throws() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> addressService.updateAddress(1L, domain("STORE", 1L)));
    }

    @Test
    void updateAddress_success() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        AddressEntity existing = entity(1L, "STORE", 10L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Address updates = domain("STORE", 10L);
        updates.setStreet("New St");
        Address result = addressService.updateAddress(1L, updates);
        assertEquals("New St", result.getStreet());
    }

    @Test
    void deleteAddress_deletesWhenPresent() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "STORE", 10L)));
        addressService.deleteAddress(1L);
        verify(addressRepository).deleteById(1L);
    }

    @Test
    void getAddressesByTypeAndReferenceId_store() {
        when(addressRepository.findByTypeAndReferenceId("STORE", 10L))
                .thenReturn(List.of(entity(1L, "STORE", 10L)));
        List<Address> result = addressService.getAddressesByTypeAndReferenceId("STORE", 10L);
        assertEquals(1, result.size());
        verify(securityContextService).validateStoreAccess(10L);
    }

    @Test
    void nonAdmin_deniedForDeliveryType() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class,
                () -> addressService.getAddressesByTypeAndReferenceId("DELIVERY", 1L));
    }

    @Test
    void nonAdmin_employeeType_validatesStoreScopes() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        MembershipEntity m = MembershipEntity.builder()
                .id(3L)
                .userId(1L)
                .businessId(2L)
                .storeScopes(Set.of(9L))
                .build();
        when(membershipRepository.findById(3L)).thenReturn(Optional.of(m));
        when(addressRepository.findByTypeAndReferenceId("EMPLOYEE", 3L)).thenReturn(List.of());

        addressService.getAddressesByTypeAndReferenceId("EMPLOYEE", 3L);
        verify(securityContextService).validateStoreAccess(9L);
    }

    @Test
    void nonAdmin_customerType_validatesStore() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        CustomerEntity c = new CustomerEntity();
        c.setId(4L);
        c.setStoreId(11L);
        when(customerRepository.findById(4L)).thenReturn(Optional.of(c));
        when(addressRepository.findByTypeAndReferenceId("CUSTOMER", 4L)).thenReturn(List.of());

        addressService.getAddressesByTypeAndReferenceId("CUSTOMER", 4L);
        verify(securityContextService).validateStoreAccess(11L);
    }

    @Test
    void nonAdmin_userType_delegatesToUserService() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        User u = new User();
        u.setId(8L);
        when(userService.getUserById(8L)).thenReturn(Optional.of(u));
        when(addressRepository.findByTypeAndReferenceId("USER", 8L)).thenReturn(List.of());

        addressService.getAddressesByTypeAndReferenceId("USER", 8L);
        verify(userService).getUserById(8L);
    }

    @Test
    void getAddressesByArea_filtersViaAdmin() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByArea_Name("Downtown")).thenReturn(List.of(entity(1L, "STORE", 1L)));
        assertEquals(1, addressService.getAddressesByArea("Downtown").size());
    }

    @Test
    void missingType_throwsAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> addressService.getAddressesByTypeAndReferenceId(null, 1L));
    }

    @Test
    void nonAdmin_filtersInaccessibleAddresses() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(addressRepository.findAll()).thenReturn(List.of(
                entity(1L, "STORE", 10L),
                entity(2L, "DELIVERY", 99L)));
        doThrow(new AccessDeniedException("denied")).when(securityContextService).validateStoreAccess(10L);

        List<Address> result = addressService.getAllAddresses();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAddressById_deniedForNonAdmin() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "DELIVERY", 1L)));

        assertThrows(AccessDeniedException.class, () -> addressService.getAddressById(1L));
    }

    @Test
    void getAddressesByPlace_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByPlace_Name("Park")).thenReturn(List.of(entity(1L, "STORE", 5L)));
        assertEquals(1, addressService.getAddressesByPlace("Park").size());
    }

    @Test
    void getAddressesByRegion_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByRegion_Name("North")).thenReturn(List.of(entity(1L, "BUSINESS", 3L)));
        assertEquals(1, addressService.getAddressesByRegion("North").size());
    }

    @Test
    void getAddressesBySubRegion_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findBySubRegion_Name("Sub")).thenReturn(List.of(entity(1L, "STORE", 2L)));
        assertEquals(1, addressService.getAddressesBySubRegion("Sub").size());
    }

    @Test
    void getAddressesByLandmark_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByLandmark_Name("Tower")).thenReturn(List.of(entity(1L, "STORE", 2L)));
        assertEquals(1, addressService.getAddressesByLandmark("Tower").size());
    }

    @Test
    void getAddressesByZipcode_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByZipcode_Code("90210")).thenReturn(List.of(entity(1L, "STORE", 2L)));
        assertEquals(1, addressService.getAddressesByZipcode("90210").size());
    }

    @Test
    void getAddressesByDistrict_filtersAccessible() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        when(addressRepository.findByDistrict_Name("Central")).thenReturn(List.of(entity(1L, "STORE", 2L)));
        assertEquals(1, addressService.getAddressesByDistrict("Central").size());
    }

    @Test
    void nonAdmin_customerType_validatesBusinessWhenNoStore() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        CustomerEntity c = new CustomerEntity();
        c.setId(4L);
        c.setBusinessId(12L);
        when(customerRepository.findById(4L)).thenReturn(Optional.of(c));
        when(addressRepository.findByTypeAndReferenceId("CUSTOMER", 4L)).thenReturn(List.of());

        addressService.getAddressesByTypeAndReferenceId("CUSTOMER", 4L);
        verify(securityContextService).validateBusinessAccess(12L);
    }

    @Test
    void nonAdmin_employeeType_validatesBusinessWhenNoStoreScopes() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        MembershipEntity m = MembershipEntity.builder()
                .id(3L).userId(1L).businessId(8L).storeScopes(Set.of()).build();
        when(membershipRepository.findById(3L)).thenReturn(Optional.of(m));
        when(addressRepository.findByTypeAndReferenceId("EMPLOYEE", 3L)).thenReturn(List.of());

        addressService.getAddressesByTypeAndReferenceId("EMPLOYEE", 3L);
        verify(securityContextService).validateBusinessAccess(8L);
    }

    @Test
    void unknownAddressType_throws() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class,
                () -> addressService.getAddressesByTypeAndReferenceId("UNKNOWN", 1L));
    }
}