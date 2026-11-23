package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="courses")
public class Course {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser mentor;
    @Column(nullable=false) public String title;
    @Column(length=2500) public String description = "";
    public String category = "Development";
    public String level = "All levels";
    public String cover = "";
    public String tags = "";
    public int lessons = 8;
    public int durationMinutes = 60;
    public long pricePaise = 49900;
    public boolean published = false;
    public double rating = 4.8;
    public Instant createdAt = Instant.now();
}
