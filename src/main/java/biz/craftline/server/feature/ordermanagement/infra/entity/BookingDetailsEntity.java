package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "booking_details")
public class BookingDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column
    private Date appointmentDate;

    @Column
    private String appointmentTime;

    @Column
    private String locationType; // STORE, HOME

    @Column
    private String address;

    @Column
    private Long staffId;

    @Column
    private String bookingStatus; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

    @Column
    private String notes;

    @Column
    private Integer durationMinutes;
}

