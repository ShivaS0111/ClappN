
package biz.craftline.server.feature.ordermanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Domain model for BookingDetails.
 * Represents service booking details for an order item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetails {

    private Long id;
    /** Store where service is booked */
    private Long storeId;
    /** Customer who booked */
    private Long customerId;
    /** Service ID being booked */
    private Long serviceId;
    /** Order item ID this booking belongs to */
    private Long orderItemId;
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
    /** Booking status: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW */
    private String bookingStatus;
    /** Additional notes */
    private String notes;
    /** Duration in minutes */
    private Integer durationMinutes;
}