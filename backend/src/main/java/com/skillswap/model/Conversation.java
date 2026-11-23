package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="conversations")
public class Conversation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public String title;
    public String type = "DIRECT";
    public Long courseId;
    @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="conversation_members",joinColumns=@JoinColumn(name="conversation_id"), inverseJoinColumns=@JoinColumn(name="user_id"))
    public Set<AppUser> members = new HashSet<>();
    public Instant createdAt = Instant.now();
}
