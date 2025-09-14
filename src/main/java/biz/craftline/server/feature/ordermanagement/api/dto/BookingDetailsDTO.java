package biz.craftline.server.feature.ordermanagement.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Data Transfer Object for BookingDetails.
 * Encapsulates service booking details for an order item.
 */
@Setter
@Getter
public class BookingDetailsDTO {
    /** Appointment date for the service */
    private Date appointmentDate;
    /** Appointment time for the service */
    private String appointmentTime;
    /** Location type: STORE, HOME */
    private String locationType;
    /** Address for home service */
    private String address;
    /** Staff ID assigned to the service */
    private Long staffId;
    /** Booking status: PENDING, CONFIRMED, COMPLETED, CANCELLED */
    private String bookingStatus;

}

