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
    private String bookingStatus;

    // Getters and setters
    // ...
}

