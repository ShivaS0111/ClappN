package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;

public class BookingDetailsEntityMapper {
    public static BookingDetailsEntity toEntity(BookingDetails model) {
        if (model == null) return null;
        BookingDetailsEntity entity = new BookingDetailsEntity();
        entity.setId(model.getId());
        entity.setStoreId(model.getStoreId());
        entity.setCustomerId(model.getCustomerId());
        entity.setServiceId(model.getServiceId());
        entity.setOrderItemId(model.getOrderItemId());
        entity.setAppointmentDate(model.getAppointmentDate());
        entity.setAppointmentTime(model.getAppointmentTime());
        entity.setLocationType(model.getLocationType());
        entity.setAddress(model.getAddress());
        entity.setStaffId(model.getStaffId());
        entity.setBookingStatus(model.getBookingStatus());
        entity.setNotes(model.getNotes());
        entity.setDurationMinutes(model.getDurationMinutes());
        return entity;
    }

    public static BookingDetails toModel(BookingDetailsEntity entity) {
        if (entity == null) return null;
        return BookingDetails.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .customerId(entity.getCustomerId())
                .serviceId(entity.getServiceId())
                .orderItemId(entity.getOrderItemId())
                .appointmentDate(entity.getAppointmentDate())
                .appointmentTime(entity.getAppointmentTime())
                .locationType(entity.getLocationType())
                .address(entity.getAddress())
                .staffId(entity.getStaffId())
                .bookingStatus(entity.getBookingStatus())
                .notes(entity.getNotes())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }
}

