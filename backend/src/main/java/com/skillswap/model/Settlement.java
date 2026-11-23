package com.skillswap.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="settlements")
public class Settlement {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Course course;
 @ManyToOne(optional=false) public AppUser buyer;
 @ManyToOne(optional=false) public AppUser mentor;
 public long basePaise; public long courseTaxPaise; public long commissionPaise; public long commissionTaxPaise; public long mentorNetPaise;
   public String source = "COURSE";
  public Instant createdAt=Instant.now();
}
