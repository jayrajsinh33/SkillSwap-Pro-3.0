package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="withdrawals")
public class Withdrawal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser user;
    public long amountPaise;
    public String bankDetails;
    public String status = "REQUESTED";
    public Instant createdAt = Instant.now();
}
