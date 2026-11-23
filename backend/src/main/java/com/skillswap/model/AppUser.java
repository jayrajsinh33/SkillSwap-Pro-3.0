package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="app_users", indexes={@Index(columnList="email", unique=true)})
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true, length=180) public String email;
    @Column(nullable=false) public String passwordHash;
    @Column(nullable=false) public String name;
    public String headline = "Lifelong learner";
    @Column(length=1000) public String bio = "";
    public String city = "India";
    public String avatar = "";
    public String skills = "";
    // Rotating login key invalidates earlier tokens on a new login or password change.
    @Column public String sessionKey = java.util.UUID.randomUUID().toString();
    public boolean freeLectureUsed = false;
    public boolean permanentlyBanned = false;
    public int performanceScore = 100;
    public int violationCount = 0;
    public long assessedPenaltyPaise = 0;
    public long hourlyRatePaise = 50000;
    public String mentorDocumentKey = "";
    public String mentorDocumentName = "";
    public boolean mentorDocumentVerified = false;
    public String mentorBankLast4 = "";
    public String mentorBankIfsc = "";
    public boolean mentorBankVerified = false;
    public String mentorAgreementVersion = "";
    public Instant mentorAgreedAt;
    public boolean mentor = false;
    public boolean mentorRequested = false;
    public boolean admin = false;
    public boolean active = true;
    public long balancePaise = 0;
    public long lifetimeEarningsPaise = 0;
    public Instant createdAt = Instant.now();
}
