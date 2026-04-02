package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.BookingDetailsDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;

public class BookingDetailsDTOMapper {
    public static BookingDetailsDTO toDTO(BookingDetails model) {
        if (model == null) return null;
        return BookingDetailsDTO.builder()
                .id(model.getId())
                .storeId(model.getStoreId())
                .customerId(model.getCustomerId())
                .serviceId(model.getServiceId())
                .orderItemId(model.getOrderItemId())
                .appointmentDate(model.getAppointmentDate())
                .appointmentTime(model.getAppointmentTime())
                .locationType(model.getLocationType())
                .address(model.getAddress())
                .staffId(model.getStaffId())
                .bookingStatus(model.getBookingStatus())
                .notes(model.getNotes())
                .durationMinutes(model.getDurationMinutes())
                .build();
    }

    public static BookingDetails fromDTO(BookingDetailsDTO dto) {
        if (dto == null) return null;
        return BookingDetails.builder()
                .id(dto.getId())
                .storeId(dto.getStoreId())
                .customerId(dto.getCustomerId())
                .serviceId(dto.getServiceId())
                .orderItemId(dto.getOrderItemId())
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .locationType(dto.getLocationType())
                .address(dto.getAddress())
                .staffId(dto.getStaffId())
                .bookingStatus(dto.getBookingStatus())
                .notes(dto.getNotes())
                .durationMinutes(dto.getDurationMinutes())
                .build();
    }
}

