package com.skillswap.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="support_tickets")
public class SupportTicket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public AppUser user;
 @Column(length=140, nullable=false) public String subject;
 @Column(length=3000, nullable=false) public String message;
   public String status="OPEN";
  public String lectureId = "";
  public String category = "GENERAL";
  public Long accusedUserId;
  public String resolution = "";
  public long refundedPaise = 0;
 public Instant createdAt=Instant.now();
}
