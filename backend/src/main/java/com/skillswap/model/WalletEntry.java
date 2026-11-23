package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="wallet_entries")
public class WalletEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser user;
    public long changePaise;
    public long balanceAfterPaise;
    public String type;
    public String description;
    public String reference;
    public Instant createdAt = Instant.now();
}
