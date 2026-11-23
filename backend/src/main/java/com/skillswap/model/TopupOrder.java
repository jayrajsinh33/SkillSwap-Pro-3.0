package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="topup_orders")
public class TopupOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser user;
    public long amountPaise;
    @Column(unique=true) public String gatewayOrderId;
    @Column(unique=true) public String gatewayPaymentId;
    public String status = "CREATED";
    public Instant createdAt = Instant.now();
}
