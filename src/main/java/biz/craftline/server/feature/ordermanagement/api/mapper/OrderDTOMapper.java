package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.*;
import biz.craftline.server.feature.ordermanagement.api.dto.*;
import biz.craftline.server.feature.paymentmanagement.api.dto.PaymentInfoDTO;
import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;

import java.util.stream.Collectors;

/**
 * Mapper for converting between Order domain models and DTOs.
 * Provides static methods for mapping Order, OrderItem, DeliveryInfo, PaymentInfo, VirtualProductDetails, and BookingDetails.
 */
public class OrderDTOMapper {
    /**
     * Converts an Order domain model to OrderDTO.
     * @param order the Order domain model
     * @return the corresponding OrderDTO
     */
    public static OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(OrderDTOMapper::toItemDTO).collect(Collectors.toList()));
        }
        if (order.getDeliveryInfo() != null) {
            dto.setDeliveryInfo(toDeliveryInfoDTO(order.getDeliveryInfo()));
        }
        if (order.getPaymentInfo() != null) {
            dto.setPaymentInfo(toPaymentInfoDTO(order.getPaymentInfo()));
        }
        return dto;
    }

    /**
     * Converts an OrderItem domain model to OrderItemDTO.
     * @param item the OrderItem domain model
     * @return the corresponding OrderItemDTO
     */
    public static OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setItemId(item.getItemIId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        if (item.getVirtualProductDetails() != null) {
            dto.setVirtualProductDetails(toVirtualProductDetailsDTO(item.getVirtualProductDetails()));
        }
        if (item.getBookingDetails() != null) {
            dto.setBookingDetails(toBookingDetailsDTO(item.getBookingDetails()));
        }
        return dto;
    }

    /**
     * Converts a DeliveryInfo domain model to DeliveryInfoDTO.
     * @param info the DeliveryInfo domain model
     * @return the corresponding DeliveryInfoDTO
     */
    public static DeliveryInfoDTO toDeliveryInfoDTO(DeliveryInfo info) {
        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        dto.setDeliveryType(info.getDeliveryType());
        dto.setAddress(info.getAddress());
        dto.setDeliveryStatus(info.getDeliveryStatus());
        dto.setTrackingNumber(info.getTrackingNumber());
        return dto;
    }

    /**
     * Converts a PaymentInfo domain model to PaymentInfoDTO.
     * @param info the PaymentInfo domain model
     * @return the corresponding PaymentInfoDTO
     */
    public static PaymentInfoDTO toPaymentInfoDTO(PaymentInfo info) {
        PaymentInfoDTO dto = new PaymentInfoDTO();
        dto.setPaymentMethod(info.getPaymentMethod());
        dto.setPaymentStatus(info.getPaymentStatus());
        dto.setTransactionId(info.getTransactionId());
        return dto;
    }

    /**
     * Converts a VirtualProductDetails domain model to VirtualProductDetailsDTO.
     * @param details the VirtualProductDetails domain model
     * @return the corresponding VirtualProductDetailsDTO
     */
    public static VirtualProductDetailsDTO toVirtualProductDetailsDTO(VirtualProductDetails details) {

        if (details == null) return null;
        VirtualProductDetailsDTO dto = new VirtualProductDetailsDTO();
        dto.setId(details.getId());
        dto.setLicenseKey(details.getLicenseKey());
        dto.setDownloadUrl(details.getDownloadUrl());
        dto.setActivationStatus(details.getActivationStatus());
        dto.setActivationDate(details.getActivationDate());
        return dto;
    }

    /**
     * Converts a BookingDetails domain model to BookingDetailsDTO.
     * @param details the BookingDetails domain model
     * @return the corresponding BookingDetailsDTO
     */
    public static BookingDetailsDTO toBookingDetailsDTO(BookingDetails details) {
        BookingDetailsDTO dto = new BookingDetailsDTO();
        dto.setAppointmentDate(details.getAppointmentDate());
        dto.setAppointmentTime(details.getAppointmentTime());
        dto.setLocationType(details.getLocationType());
        dto.setAddress(details.getAddress());
        dto.setStaffId(details.getStaffId());
        dto.setBookingStatus(details.getBookingStatus());
        return dto;
    }

    /**
     * Converts an OrderDTO to Order domain model.
     */
    public static Order fromDTO(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setCustomerId(dto.getCustomerId());
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(dto.getStatus());
        if (dto.getItems() != null) {
            order.setItems(dto.getItems().stream().map(OrderDTOMapper::fromItemDTO).collect(Collectors.toList()));
        }
        if (dto.getDeliveryInfo() != null) {
            order.setDeliveryInfo(fromDeliveryInfoDTO(dto.getDeliveryInfo()));
        }
        if (dto.getPaymentInfo() != null) {
            order.setPaymentInfo(fromPaymentInfoDTO(dto.getPaymentInfo()));
        }
        return order;
    }

    public static OrderItem fromItemDTO(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setId(dto.getId());
        item.setItemType(dto.getItemType());
        item.setItemIId(dto.getItemId());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        if (dto.getVirtualProductDetails() != null) {
            item.setVirtualProductDetails(fromVirtualProductDetailsDTO(dto.getVirtualProductDetails()));
        }
        if (dto.getBookingDetails() != null) {
            item.setBookingDetails(fromBookingDetailsDTO(dto.getBookingDetails()));
        }
        return item;
    }

    public static DeliveryInfo fromDeliveryInfoDTO(DeliveryInfoDTO dto) {
        DeliveryInfo info = new DeliveryInfo();
        info.setDeliveryType(dto.getDeliveryType());
        info.setAddress(dto.getAddress());
        info.setDeliveryStatus(dto.getDeliveryStatus());
        info.setTrackingNumber(dto.getTrackingNumber());
        return info;
    }

    public static PaymentInfo fromPaymentInfoDTO(PaymentInfoDTO dto) {
        PaymentInfo info = new PaymentInfo();
        info.setPaymentMethod(dto.getPaymentMethod());
        info.setPaymentStatus(dto.getPaymentStatus());
        info.setTransactionId(dto.getTransactionId());
        return info;
    }

    public static VirtualProductDetails fromVirtualProductDetailsDTO(VirtualProductDetailsDTO dto) {
        if (dto == null) return null;
        VirtualProductDetails details = new VirtualProductDetails();
        details.setId(dto.getId());
        details.setLicenseKey(dto.getLicenseKey());
        details.setDownloadUrl(dto.getDownloadUrl());
        details.setActivationStatus(dto.getActivationStatus());
        details.setActivationDate(dto.getActivationDate());
        return details;
    }

    public static BookingDetails fromBookingDetailsDTO(BookingDetailsDTO dto) {
        BookingDetails details = new BookingDetails();
        details.setAppointmentDate(dto.getAppointmentDate());
        details.setAppointmentTime(dto.getAppointmentTime());
        details.setLocationType(dto.getLocationType());
        details.setAddress(dto.getAddress());
        details.setStaffId(dto.getStaffId());
        details.setBookingStatus(dto.getBookingStatus());
        return details;
    }

    // Add fromDTO methods as needed for request handling
}
