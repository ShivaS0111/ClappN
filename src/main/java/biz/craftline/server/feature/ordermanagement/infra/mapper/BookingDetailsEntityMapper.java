package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;

public class BookingDetailsEntityMapper {
    public static BookingDetailsEntity toEntity(BookingDetails model) {
        if (model == null) return null;
        BookingDetailsEntity entity = new BookingDetailsEntity();
        entity.setAppointmentDate(model.getAppointmentDate());
        entity.setAppointmentTime(model.getAppointmentTime());
        entity.setLocationType(model.getLocationType());
        entity.setAddress(model.getAddress());
        entity.setStaffId(model.getStaffId());
        entity.setBookingStatus(model.getBookingStatus());
        return entity;
    }

    public static BookingDetails toModel(BookingDetailsEntity entity) {
        if (entity == null) return null;
        BookingDetails model = new BookingDetails();
        model.setAppointmentDate(entity.getAppointmentDate());
        model.setAppointmentTime(entity.getAppointmentTime());
        model.setLocationType(entity.getLocationType());
        model.setAddress(entity.getAddress());
        model.setStaffId(entity.getStaffId());
        model.setBookingStatus(entity.getBookingStatus());
        return model;
    }
}

