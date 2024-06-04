package org.kostrikov.util;

import jakarta.transaction.Transactional;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.kostrikov.entity.Course;
import org.kostrikov.entity.Student;
import org.kostrikov.entity.StudentProfile;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DataLoader {

    public static void loadData(SessionFactory sessionFactory) {
        @Cleanup Session session = sessionFactory.openSession();

        Course javaEnterprise = createCourse("Java Enterprise");
        Course javaHibernate = createCourse("Java Hibernate");
        Course javaSpring = createCourse("Java Spring");
        Course react = createCourse("React");
        Course sqlAndNoSqlDatabases = createCourse("SQL and NoSql databases");
        Course bigData = createCourse("Big Data");

        Student student = createStudent("Дмитрий Петров", StudentProfile.builder().grade(9.40).attendance(100).build());
        Student student1 = createStudent("Екатерина Смирнова", StudentProfile.builder().grade(8.45).attendance(80).build());
        Student student2 = createStudent("Ольга Соколова", StudentProfile.builder().grade(5.25).attendance(65).build());
        Student student3 = createStudent("Андрей Козлов", StudentProfile.builder().grade(4.00).attendance(90).build());
        Student student4 = createStudent("Наталья Морозова", StudentProfile.builder().grade(8.00).attendance(90).build());
        Student student5 = createStudent("Игорь Николаев", StudentProfile.builder().grade(4.00).attendance(20).build());
        Student student6 = createStudent("Мария Павлова", StudentProfile.builder().grade(4.65).attendance(50).build());
        Student student7 = createStudent("Владимир Федоров", StudentProfile.builder().grade(4.33).attendance(40).build());
        Student student8 = createStudent("Елена Кузнецова", StudentProfile.builder().grade(4.15).attendance(43).build());
        Student student9 = createStudent("Ольга Козлова", StudentProfile.builder().grade(5.00).attendance(90).build());
        Student student10 = createStudent("Сергей Кузнецов", StudentProfile.builder().grade(3.55).attendance(34).build());
        Student student11 = createStudent("Иван Соколов", StudentProfile.builder().grade(8.65).attendance(70).build());
        Student student12 = createStudent("Владимир Лебедев", StudentProfile.builder().grade(6.10).attendance(34).build());
        Student student13 = createStudent("Екатерина Петрова", StudentProfile.builder().grade(6.00).attendance(44).build());
        Student student14 = createStudent("Анна Михайлова", StudentProfile.builder().grade(3.34).attendance(35).build());
        Student student15 = createStudent("Руслан Хабибуллин", StudentProfile.builder().grade(10.00).attendance(100).build());

        session.beginTransaction();
        persistRelationshipStudentsAndCourse(session, javaEnterprise, student, student2, student3);
        persistRelationshipStudentsAndCourse(session, javaHibernate, student4, student5, student6, student10);
        persistRelationshipStudentsAndCourse(session, javaSpring, student7, student8);
        persistRelationshipStudentsAndCourse(session, react, student9, student1);
        persistRelationshipStudentsAndCourse(session, sqlAndNoSqlDatabases, student11, student12, student13, student14);
        persistRelationshipStudentsAndCourse(session, bigData, student15);
        session.getTransaction().commit();
    }

    private static void persistRelationshipStudentsAndCourse(Session session, Course course, Student... students) {
        for (Student student : students) {
            course.addStudent(student);
        }
        session.persist(course);
    }


    public static Course createCourse(String name) {
        return Course.builder()
                .name(name)
                .build();
    }

    public static Student createStudent(String name, StudentProfile profile) {
        return Student.builder().name(name).profile(profile).build();
    }


}
