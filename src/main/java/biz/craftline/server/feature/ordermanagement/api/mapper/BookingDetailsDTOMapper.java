package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.BookingDetailsDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;

public class BookingDetailsDTOMapper {
    public static BookingDetailsDTO toDTO(BookingDetails entity) {
        if (entity == null) return null;
        BookingDetailsDTO dto = new BookingDetailsDTO();
        dto.setAppointmentDate(entity.getAppointmentDate());
        dto.setAppointmentTime(entity.getAppointmentTime());
        dto.setLocationType(entity.getLocationType());
        dto.setAddress(entity.getAddress());
        dto.setStaffId(entity.getStaffId());
        dto.setBookingStatus(entity.getBookingStatus());
        return dto;
    }

    public static BookingDetails fromDTO(BookingDetailsDTO dto) {
        if (dto == null) return null;
        BookingDetails entity = new BookingDetails();
        entity.setAppointmentDate(dto.getAppointmentDate());
        entity.setAppointmentTime(dto.getAppointmentTime());
        entity.setLocationType(dto.getLocationType());
        entity.setAddress(dto.getAddress());
        entity.setStaffId(dto.getStaffId());
        entity.setBookingStatus(dto.getBookingStatus());
        return entity;
    }
}

