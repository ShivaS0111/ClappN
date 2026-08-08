package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.VirtualProductDetailsService;
import biz.craftline.server.feature.ordermanagement.infra.entity.VirtualProductDetailsEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.VirtualProductDetailsEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.VirtualProductDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VirtualProductDetailsServiceImpl implements VirtualProductDetailsService {
    private final VirtualProductDetailsRepository repository;
    private final SecurityContextService securityContextService;

    @Autowired
    public VirtualProductDetailsServiceImpl(VirtualProductDetailsRepository repository,
                                            SecurityContextService securityContextService) {
        this.repository = repository;
        this.securityContextService = securityContextService;
    }

    @Override
    public List<VirtualProductDetails> getAllVirtualProductDetails() {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Listing all virtual product details requires SYSTEM_ADMIN");
        }
        return repository.findAll().stream()
                .map(VirtualProductDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public VirtualProductDetails getVirtualProductDetails(Long id) {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Direct virtual product details access requires SYSTEM_ADMIN");
        }
        return repository.findById(id)
                .map(VirtualProductDetailsEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public VirtualProductDetails addVirtualProductDetails(VirtualProductDetails details) {
        VirtualProductDetailsEntity entity = VirtualProductDetailsEntityMapper.toEntity(details);
        VirtualProductDetailsEntity saved = repository.save(entity);
        return VirtualProductDetailsEntityMapper.toModel(saved);
    }

    @Override
    public VirtualProductDetails updateVirtualProductDetails(Long id, VirtualProductDetails details) {
        if (!repository.existsById(id)) return null;
        VirtualProductDetailsEntity entity = VirtualProductDetailsEntityMapper.toEntity(details);
        entity.setId(id);
        VirtualProductDetailsEntity saved = repository.save(entity);
        return VirtualProductDetailsEntityMapper.toModel(saved);
    }

    @Override
    public void deleteVirtualProductDetails(Long id) {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Deleting virtual product details requires SYSTEM_ADMIN");
        }
        repository.deleteById(id);
    }
}
