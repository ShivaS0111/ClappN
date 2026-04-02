package biz.craftline.server.feature.ordermanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data Transfer Object for BookingDetails.
 * Encapsulates service booking details for an order item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsDTO {
    private Long id;
    private Long storeId;
    private Long customerId;
    private Long serviceId;
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
    /** Notes */
    private String notes;
    /** Duration in minutes */
    private Integer durationMinutes;
}

