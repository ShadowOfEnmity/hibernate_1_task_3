import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.junit.jupiter.api.*;
import org.kostrikov.entity.Course;
import org.kostrikov.entity.Student;
import org.kostrikov.entity.StudentProfile;
import org.kostrikov.entity.Trainer;
import org.kostrikov.util.DataCleaner;
import org.kostrikov.util.DataLoader;
import org.kostrikov.utils.HibernateConfiguration;

import java.util.*;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RunnerTest {

    private final SessionFactory sessionFactory = HibernateConfiguration.buildSessionFactory();

    @BeforeEach
    void initialDatabase() {
        DataLoader.loadData(sessionFactory);
    }

    @Test
    void TestGetStudentsFromJavaEE() {
        Set<Student> expectedStudents = new HashSet<>();
        expectedStudents.add(Student.builder().name("Дмитрий Петров").build());
        expectedStudents.add(Student.builder().name("Ольга Соколова").build());
        expectedStudents.add(Student.builder().name("Андрей Козлов").build());

        @Cleanup Session currentSession = sessionFactory.openSession();
        currentSession.beginTransaction();
        Query<Course> q = currentSession.createQuery("FROM Course c INNER JOIN FETCH c.students u where c.name LIKE :name", Course.class);
        q.setParameter("name", "%Java Enterprise%");
        List<Course> resultList = q.getResultList();

        Set<Student> students = resultList.stream().flatMap(course -> course.getStudents().stream()).collect(Collectors.toSet());

        currentSession.getTransaction().commit();

        Assertions.assertTrue(() -> students.equals(expectedStudents), () -> "No students found in java EE course");
    }

    @Test
    void TestDeleteEnterpriseCourseStudentsWithAvgGPALessThan6() {
        Set<Long> expectedDeleted = new HashSet<>();
        Set<Long> actualDeleted = new HashSet<>();
        List<Student> actualResultList = new ArrayList<>();

        @Cleanup Session currentSession = sessionFactory.openSession();
        currentSession.beginTransaction();
        HibernateCriteriaBuilder cb = currentSession.getCriteriaBuilder();

        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        Root<Student> studentRoot = cq.from(Student.class);

        studentRoot.fetch("course", JoinType.INNER);
        studentRoot.fetch("profile", JoinType.INNER);

        cq.select(studentRoot).where(cb.and(cb.like(studentRoot.get("course").get("name"), "%Enterprise%"), cb.lessThan(studentRoot.get("profile").get("grade"), 6.00D)));
        List<Student> resultList = currentSession.createQuery(cq).getResultList();

        for (Student student : resultList) {
            expectedDeleted.add(student.getId());
            currentSession.remove(student);
            currentSession.flush();
            Optional.ofNullable(currentSession.get(Student.class, student.getId())).ifPresentOrElse(students -> {
            }, () -> actualDeleted.add(student.getId()));
        }

        actualResultList.addAll(resultList);
        currentSession.getTransaction().commit();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, actualResultList.size(), () -> "No students found in java EE course by condition"),
                () -> Assertions.assertTrue(() -> !actualDeleted.isEmpty() && actualDeleted.containsAll(expectedDeleted) && expectedDeleted.containsAll(actualDeleted), () -> "Not all relevant students were removed")
        );
    }

    @Test
    void TestAddStudentToJavaEnterpriseCourse() {

        Set<Student> students = new HashSet<>();
        Student newStudent = Student.builder().name("Андрей Шуйков").profile(
                StudentProfile.builder().grade(10.00).attendance(100).build()
        ).build();

        @Cleanup Session currentSession = sessionFactory.openSession();
        currentSession.beginTransaction();

        Query<Course> q = currentSession.createQuery("FROM Course c INNER JOIN FETCH c.students u where c.name LIKE :name", Course.class);
        q.setParameter("name", "%Java Enterprise%");
        List<Course> resultList = q.getResultList();

        if (resultList.size() == 1) {
            Course course = resultList.get(0);
            course.addStudent(newStudent);
            students.addAll(currentSession.merge(course).getStudents());
        }

        currentSession.getTransaction().commit();


        Assertions.assertAll(
                () -> Assertions.assertTrue(() -> resultList.size() == 1),
                () -> Assertions.assertTrue(() -> students.contains(newStudent))
        );

    }

    @Test
    void TestDeleteEnterpriseCourse() {
        @Cleanup Session session = sessionFactory.openSession();

        session.beginTransaction();

        HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> from = cq.from(Course.class);
        cq.select(from).where(cb.like(from.get("name"), "%Enterprise%"));
        List<Course> resultList = session.createQuery(cq).getResultList();

        long id = -1;
        if (resultList.size() == 1) {
            Course course = resultList.get(0);
            session.remove(course);
            id = course.getId();
        }
        session.getTransaction().commit();

        final long deletedId = id;
        Assertions.assertAll(
                () -> Assertions.assertTrue(() -> resultList.size() == 1),
                () -> Assertions.assertNull(session.get(Course.class, deletedId))
        );

    }

    @Test
    void TestPersistTrainer() {
        List<Course> expectedCourses = new ArrayList<>();
        Trainer trainer = Trainer.builder().name("Андрей Тимофеев").build();
        Course course1 = Course.builder().name("Test course 1").build();
        Course course2 = Course.builder().name("Test course 2").build();
        trainer.addCourse(course2);
        expectedCourses.add(course1);
        expectedCourses.add(course2);
        trainer.addCourse(course1);

        @Cleanup Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        session.persist(trainer);
        session.flush();
        session.getTransaction().commit();

        var actualTrainer = Optional.ofNullable(session.get(Trainer.class, trainer.getId()));
        var actualCourses = actualTrainer.map(Trainer::getCourses).orElse(new ArrayList<>());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(actualTrainer.get()),
                () -> Assertions.assertEquals(2, actualCourses.size()),
                () -> Assertions.assertTrue(actualCourses.containsAll(expectedCourses))
        );

    }

    @Test
    void TestToChangeCourse() {

        Course course1 = Course.builder().name("Test course 1").build();

        @Cleanup Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        session.persist(course1);
        session.flush();
        session.getTransaction().commit();

        session.getTransaction().begin();
        Course courseAfterChange = session.get(Course.class, course1.getId());
        courseAfterChange.setName(courseAfterChange.getName() + " after change");
        session.getTransaction().commit();

        Assertions.assertEquals("Test course 1 after change", courseAfterChange.getName());

    }

    @AfterEach
    void clearTables() {
        DataCleaner.cleanupTables(sessionFactory);
    }

    @AfterAll
    void finish() {
        sessionFactory.close();
    }
}
