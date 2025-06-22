package biz.craftline.server.feature.address.infra.entity;

import jakarta.persistence.*;

@Entity(name="address")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;



}
