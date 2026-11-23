package com.skillswap.model;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="group_classes")
public class GroupClass {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Course course;
 @ManyToOne(optional=false) public AppUser mentor;
 @Column(nullable=false) public String title;
 @Column(length=1500) public String description="";
 public Instant startAt;
 @Column(unique=true) public String lectureId;
 public int durationMinutes=60;
 public int capacity=30;
 @Column(nullable=false,unique=true) public String roomName;
 @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="group_class_attendees",joinColumns=@JoinColumn(name="group_class_id"),inverseJoinColumns=@JoinColumn(name="user_id"))
 public Set<AppUser> attendees=new HashSet<>();
 public Instant createdAt=Instant.now();
}
