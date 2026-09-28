package biz.craftline.server.feature.membership.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.membership.api.dto.MembershipRequest;
import biz.craftline.server.feature.membership.api.dto.MembershipResponse;
import biz.craftline.server.feature.membership.api.mapper.MembershipMapper;
import biz.craftline.server.feature.membership.domain.model.Membership;
import biz.craftline.server.feature.membership.domain.service.MembershipService;
import biz.craftline.server.util.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<MembershipResponse>>> list() {
        List<MembershipResponse> list = membershipService.listAccessible().stream()
                .map(MembershipMapper::toResponse)
                .toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/{id}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<MembershipResponse>> get(@PathVariable Long id) {
        try {
            return APIResponse.ok(MembershipMapper.toResponse(membershipService.getById(id)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/business/{businessId}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<MembershipResponse>>> byBusiness(@PathVariable Long businessId) {
        List<MembershipResponse> list = membershipService.listByBusiness(businessId).stream()
                .map(MembershipMapper::toResponse)
                .toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/user/{userId}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<MembershipResponse>>> byUser(@PathVariable Long userId) {
        List<MembershipResponse> list = membershipService.listByUser(userId).stream()
                .map(MembershipMapper::toResponse)
                .toList();
        return APIResponse.ok(list);
    }

    @PostMapping
    @RequirePermission("user.create")
    public ResponseEntity<APIResponse<MembershipResponse>> create(@RequestBody MembershipRequest request) {
        try {
            Membership created = membershipService.create(MembershipMapper.toDomain(request));
            return APIResponse.ok(MembershipMapper.toResponse(created));
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequirePermission("user.update")
    public ResponseEntity<APIResponse<MembershipResponse>> update(
            @PathVariable Long id,
            @RequestBody MembershipRequest request) {
        try {
            Membership updated = membershipService.update(id, MembershipMapper.toDomain(request));
            return APIResponse.ok(MembershipMapper.toResponse(updated));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/{id}/deactivate")
    @RequirePermission("user.update")
    public ResponseEntity<APIResponse<MembershipResponse>> deactivate(@PathVariable Long id) {
        try {
            return APIResponse.ok(MembershipMapper.toResponse(membershipService.deactivate(id)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/activate")
    @RequirePermission("user.update")
    public ResponseEntity<APIResponse<MembershipResponse>> activate(@PathVariable Long id) {
        try {
            return APIResponse.ok(MembershipMapper.toResponse(membershipService.activate(id)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
