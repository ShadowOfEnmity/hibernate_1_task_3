package org.kostrikov.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"courses"})
@EqualsAndHashCode(of = "name")
@Builder
@Entity
@Table(name = "trainer")
public class Trainer {
    @Id
    private long id;

    @NotNull
    @Column(nullable = false, length = 65)
    private String name;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "course_trainer", joinColumns = @JoinColumn(name = "trainer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id"))

    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course){
        courses.add(course);
        course.getTrainers().add(this);
    }
}
