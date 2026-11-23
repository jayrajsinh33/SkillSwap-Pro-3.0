package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public Conversation conversation;
    @ManyToOne(optional=false) public AppUser sender;
    @Column(length=4000) public String body = "";
    public Long attachmentId;
    public Instant createdAt = Instant.now();
}
