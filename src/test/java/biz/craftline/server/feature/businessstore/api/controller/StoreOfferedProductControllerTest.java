package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedProductDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedProductRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreOfferedProductControllerTest {

    @Mock private StoreOfferedProductDTOMapper productMapper;
    @Mock private StoreItemPriceDTOMapper priceMapper;
    @Mock private ProductsOfferedByStoreService storeOfferedProductService;
    @Mock private StoreItemPriceService priceHandleService;
    @Mock private StoreItemPriceService storeItemPriceService;

    private StoreOfferedProductController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new StoreOfferedProductController(
                productMapper, priceMapper, storeOfferedProductService, priceHandleService, storeItemPriceService);
    }

    @Test
    void list_returnsProducts() {
        StoreOfferedProduct product = StoreOfferedProduct.builder().id(1L).storeId(1L).build();
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        when(storeOfferedProductService.findAll()).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> response = controller.list();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void storeOfferedProductsByProductId() {
        StoreOfferedProduct product = StoreOfferedProduct.builder().id(1L).build();
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        when(storeOfferedProductService.findById(1L)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(dto);

        ResponseEntity<APIResponse<StoreOfferedProductDTO>> response =
                controller.storeOfferedProductsByProductId(1L);
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void search_andSearchByStore() {
        StoreOfferedProduct product = StoreOfferedProduct.builder().id(1L).build();
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        when(storeOfferedProductService.searchProductByKeyword("soap")).thenReturn(List.of(product));
        when(storeOfferedProductService.searchProductByStoreIdAndKeyword(1L, "soap")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        assertEquals(1, controller.search("soap").getBody().getData().size());
        assertEquals(1, controller.searchProductByStoreIdAndKeyword("soap", 1L).getBody().getData().size());
    }

    @Test
    void storeOfferedProductsByBusinessAndStore() {
        StoreOfferedProduct product = StoreOfferedProduct.builder().id(1L).build();
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        when(storeOfferedProductService.findProductsByBusinessId(10L)).thenReturn(Optional.of(List.of(product)));
        when(storeOfferedProductService.findProductsByStoreId(1L)).thenReturn(Optional.of(List.of(product)));
        when(productMapper.toDTO(product)).thenReturn(dto);

        assertEquals(1, controller.storeOfferedProductsByBusinessId(10L).getBody().getData().size());
        assertEquals(1, controller.storeOfferedProducts(1L).getBody().getData().size());
    }

    @Test
    void save_singleAndBatch() {
        AddNewStoreOfferedProductRequest req = new AddNewStoreOfferedProductRequest();
        req.setStoreId(1L);
        StoreOfferedProduct domain = StoreOfferedProduct.builder().id(1L).storeId(1L).build();
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        when(productMapper.toDomain(req)).thenReturn(domain);
        when(storeOfferedProductService.save(domain)).thenReturn(domain);
        when(storeOfferedProductService.save(List.of(domain))).thenReturn(List.of(domain));
        when(productMapper.toDTO(domain)).thenReturn(dto);

        ResponseEntity<APIResponse<StoreOfferedProductDTO>> created = controller.save(req);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> batch = controller.save(List.of(req));
        assertEquals(HttpStatus.CREATED, batch.getStatusCode());

        assertEquals(HttpStatus.BAD_REQUEST, controller.save(List.of()).getStatusCode());
    }

    @Test
    void priceList() {
        StoreItemPrice price = StoreItemPrice.builder().id(1L).build();
        StoreItemPriceDTO dto = new StoreItemPriceDTO();
        when(priceHandleService.findAllByLotId(5L)).thenReturn(List.of(price));
        when(priceMapper.toDTO(price)).thenReturn(dto);

        ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> response = controller.priceList(5L);
        assertEquals(1, response.getBody().getData().size());
    }
}
