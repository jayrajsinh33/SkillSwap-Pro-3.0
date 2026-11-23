package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="material_files")
public class MaterialFile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser owner;
    public Long courseId;
    public Long conversationId;
    public String filename;
    public String mime;
    public String storageKey;
    public long bytes;
    public Instant createdAt = Instant.now();
}
