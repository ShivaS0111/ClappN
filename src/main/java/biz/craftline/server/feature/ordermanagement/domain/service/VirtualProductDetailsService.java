package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import java.util.List;

public interface VirtualProductDetailsService {
    List<VirtualProductDetails> getAllVirtualProductDetails();
    VirtualProductDetails getVirtualProductDetails(Long id);
    VirtualProductDetails addVirtualProductDetails(VirtualProductDetails details);
    VirtualProductDetails updateVirtualProductDetails(Long id, VirtualProductDetails details);
    void deleteVirtualProductDetails(Long id);
}

