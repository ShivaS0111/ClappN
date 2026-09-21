package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.DeliveryInfoDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.DeliveryInfoService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryInfoControllerTest {

    @Mock private DeliveryInfoService deliveryInfoService;
    @Mock private DeliveryInfoDTOMapper deliveryInfoDTOMapper;

    @InjectMocks
    private DeliveryInfoController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllDeliveryInfo() {
        DeliveryInfo info = new DeliveryInfo();
        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        when(deliveryInfoService.getAllDeliveryInfo()).thenReturn(List.of(info));
        when(deliveryInfoDTOMapper.toDTO(info)).thenReturn(dto);

        ResponseEntity<APIResponse<List<DeliveryInfoDTO>>> response = controller.getAllDeliveryInfo();
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getDeliveryInfo() {
        DeliveryInfo info = new DeliveryInfo();
        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        when(deliveryInfoService.getDeliveryInfo(1L)).thenReturn(info);
        when(deliveryInfoDTOMapper.toDTO(info)).thenReturn(dto);
        when(deliveryInfoService.getDeliveryInfo(9L)).thenReturn(null);

        assertNotNull(controller.getDeliveryInfo(1L).getBody().getData());
        assertNull(controller.getDeliveryInfo(9L).getBody().getData());
    }

    @Test
    void addAndUpdateDeliveryInfo() {
        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        DeliveryInfo domain = new DeliveryInfo();
        DeliveryInfo saved = new DeliveryInfo();
        when(deliveryInfoDTOMapper.fromDTO(dto)).thenReturn(domain);
        when(deliveryInfoService.addDeliveryInfo(domain)).thenReturn(saved);
        when(deliveryInfoService.updateDeliveryInfo(1L, domain)).thenReturn(saved);
        when(deliveryInfoService.updateDeliveryInfo(9L, domain)).thenReturn(null);
        when(deliveryInfoDTOMapper.toDTO(saved)).thenReturn(dto);

        assertNotNull(controller.addDeliveryInfo(dto).getBody().getData());
        assertNotNull(controller.updateDeliveryInfo(1L, dto).getBody().getData());
        assertNull(controller.updateDeliveryInfo(9L, dto).getBody().getData());
    }

    @Test
    void deleteDeliveryInfo() {
        controller.deleteDeliveryInfo(1L);
        verify(deliveryInfoService).deleteDeliveryInfo(1L);
    }
}
