package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="bookings")
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public Course course;
    @ManyToOne(optional=false) public AppUser learner;
    @ManyToOne(optional=false) public AppUser mentor;
    public String status = "PENDING";
    public Instant startAt;
        public int durationMinutes = 60;
    public long paidPaise = 0;
    public boolean freeTrial = false;
    public boolean refunded = false;
    @Column(unique=true) public String lectureId;
    public String bundleId = "";
    @Column(nullable=false,unique=true) public String roomName;
    public Instant createdAt = Instant.now();
}
