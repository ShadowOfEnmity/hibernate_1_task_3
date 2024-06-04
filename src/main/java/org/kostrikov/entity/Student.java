package org.kostrikov.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "course")
@EqualsAndHashCode(of = "name")
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(nullable = false, length = 65)
    private String name;

    @ManyToOne
    @JoinColumn(name = "course_id", unique = false, nullable = true)
    private Course course;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id")
    private StudentProfile profile;
}
