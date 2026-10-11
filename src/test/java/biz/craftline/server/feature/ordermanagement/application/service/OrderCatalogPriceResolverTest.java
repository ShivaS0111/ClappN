package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCatalogPriceResolverTest {

    @Mock ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    @Mock ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    @Mock BusinessProductJpaRepository businessProductJpaRepository;
    @Mock BusinessServicesJpaRepository businessServicesJpaRepository;
    @Mock StoreItemPriceHandleRepository storeItemPriceHandleRepository;

    @InjectMocks OrderCatalogPriceResolver resolver;

    @Test
    void storeOverrideWinsOverBusinessDefault() {
        StoreOfferedProductEntity offering = new StoreOfferedProductEntity();
        offering.setId(50L);
        offering.setStoreId(1L);
        offering.setBusinessProductId(10L);
        when(productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(1L, 10L))
                .thenReturn(Optional.of(offering));

        StoreItemPriceEntity price = StoreItemPriceEntity.builder()
                .itemId(50L)
                .itemType(Item.ProductLot.getType())
                .price(180.0)
                .build();
        when(storeItemPriceHandleRepository.findActivePrices(eq(Item.ProductLot.getType()), eq(50L), any(Pageable.class)))
                .thenReturn(List.of(price));

        OrderItem item = OrderItem.builder().itemType(1L).itemIId(10L).build();
        Optional<Double> resolved = resolver.resolveUnitPrice(1L, item);
        assertEquals(Optional.of(180.0), resolved);
    }

    @Test
    void fallsBackToBusinessDefault() {
        StoreOfferedProductEntity offering = new StoreOfferedProductEntity();
        offering.setId(50L);
        offering.setStoreId(1L);
        offering.setBusinessProductId(10L);
        when(productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(1L, 10L))
                .thenReturn(Optional.of(offering));
        when(storeItemPriceHandleRepository.findActivePrices(anyLong(), eq(50L), any(Pageable.class)))
                .thenReturn(List.of());

        BusinessProductEntity master = new BusinessProductEntity();
        master.setId(10L);
        master.setAmount(200f);
        when(businessProductJpaRepository.findById(10L)).thenReturn(Optional.of(master));

        OrderItem item = OrderItem.builder().itemType(1L).itemIId(10L).build();
        assertEquals(Optional.of(200.0), resolver.resolveUnitPrice(1L, item));
    }

    @Test
    void emptyWhenNoCatalogPrice() {
        when(productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(1L, 10L))
                .thenReturn(Optional.empty());
        when(productsOfferedByStoreRepository.findById(10L)).thenReturn(Optional.empty());
        when(businessProductJpaRepository.findById(10L)).thenReturn(Optional.empty());

        OrderItem item = OrderItem.builder().itemType(1L).itemIId(10L).build();
        assertTrue(resolver.resolveUnitPrice(1L, item).isEmpty());
    }
}
