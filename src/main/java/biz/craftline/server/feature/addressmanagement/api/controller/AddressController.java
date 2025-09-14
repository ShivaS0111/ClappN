package biz.craftline.server.feature.addressmanagement.api.controller;

import biz.craftline.server.feature.addressmanagement.api.request.AddressRequest;
import biz.craftline.server.feature.addressmanagement.api.response.AddressResponse;
import biz.craftline.server.feature.addressmanagement.api.mapper.AddressMapper;
import biz.craftline.server.feature.addressmanagement.domain.model.Address;
import biz.craftline.server.feature.addressmanagement.domain.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping
    public List<AddressResponse> getAllAddresses() {
        return addressService.getAllAddresses().stream().map(AddressMapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AddressResponse getAddressById(@PathVariable Long id) {
        Address address = addressService.getAddressById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        return AddressMapper.toResponse(address);
    }

    @GetMapping("/search")
    public List<AddressResponse> getAddressesByTypeAndReferenceId(@RequestParam String type, @RequestParam Long referenceId) {
        return addressService.getAddressesByTypeAndReferenceId(type, referenceId).stream().map(AddressMapper::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public AddressResponse createAddress(@RequestBody AddressRequest request) {
        Address address = AddressMapper.toDomain(request);
        Address created = addressService.createAddress(address);
        return AddressMapper.toResponse(created);
    }

    @PutMapping("/{id}")
    public AddressResponse updateAddress(@PathVariable Long id, @RequestBody AddressRequest request) {
        Address address = AddressMapper.toDomain(request);
        Address updated = addressService.updateAddress(id, address);
        return AddressMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
    }
}

