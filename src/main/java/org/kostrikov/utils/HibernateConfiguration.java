package org.kostrikov.utils;

import lombok.experimental.UtilityClass;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.kostrikov.entity.Course;
import org.kostrikov.entity.Student;
import org.kostrikov.entity.StudentProfile;
import org.kostrikov.entity.Trainer;

@UtilityClass
public class HibernateConfiguration {
    public static SessionFactory buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAnnotatedClass(Course.class);
        configuration.addAnnotatedClass(Student.class);
        configuration.addAnnotatedClass(StudentProfile.class);
        configuration.addAnnotatedClass(Trainer.class);
        return configuration.configure().buildSessionFactory();
    }
}
