package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.VirtualProductDetailsService;
import biz.craftline.server.feature.ordermanagement.infra.entity.VirtualProductDetailsEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.VirtualProductDetailsEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.VirtualProductDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VirtualProductDetailsServiceImpl implements VirtualProductDetailsService {
    private final VirtualProductDetailsRepository repository;

    @Autowired
    public VirtualProductDetailsServiceImpl(VirtualProductDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<VirtualProductDetails> getAllVirtualProductDetails() {
        return repository.findAll().stream()
                .map(VirtualProductDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public VirtualProductDetails getVirtualProductDetails(Long id) {
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
        repository.deleteById(id);
    }
}

