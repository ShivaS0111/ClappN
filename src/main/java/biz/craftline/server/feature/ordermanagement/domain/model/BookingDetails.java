
package biz.craftline.server.feature.ordermanagement.domain.model;

import lombok.Data;

import java.util.Date;

/**
 * Domain model for BookingDetails.
 * Represents service booking details for an order item.
 */
@Data
public class BookingDetails {

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