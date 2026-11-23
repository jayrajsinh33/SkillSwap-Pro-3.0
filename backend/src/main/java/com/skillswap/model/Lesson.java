
package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "course_lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    public Course course;

    public int position;

    @Column(nullable = false)
    public String title;

    @Column(length = 8000)
    public String content = "";

    public int estimatedMinutes = 20;

    public Long videoFileId;

    public Long notesFileId;

    public String videoUrl = "";

    public Instant createdAt = Instant.now();
}
