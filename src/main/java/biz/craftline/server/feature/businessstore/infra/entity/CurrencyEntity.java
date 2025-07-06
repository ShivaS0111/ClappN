package biz.craftline.server.feature.businessstore.infra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity(name = "currency")
public class CurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3, unique = true, nullable = false)
    private String code; // e.g., "INR", "USD", "EUR"

    @Column(nullable = false)
    private String name; // e.g., "Indian Rupee"

    @Column(length = 5)
    private String symbol; // ₹, $, €

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}

