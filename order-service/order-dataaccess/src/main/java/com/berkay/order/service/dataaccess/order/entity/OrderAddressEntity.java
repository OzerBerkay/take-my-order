package com.berkay.order.service.dataaccess.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_address")
@Entity
public class OrderAddressEntity {
    @Id
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL) //By this if order removes this will too
    @JoinColumn(name = "ORDER_ID")
    private OrderEntity order;

    private String city;
    private String district;
    private String neighborhood;
    private String street;
    private String buildingNumber;
    private String doorNumber;
    private Integer floor;
    private String addressInstructions;
    private String contactFirstName;
    private String contactLastName;
    private String contactPhone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderAddressEntity that = (OrderAddressEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}