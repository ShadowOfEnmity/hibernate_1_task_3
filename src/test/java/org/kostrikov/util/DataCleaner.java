package org.kostrikov.util;

import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.kostrikov.entity.Student;

import java.util.Set;

@UtilityClass
public class DataCleaner {

    public static void cleanupTables(SessionFactory sessionFactory) {
        @Cleanup Session session = sessionFactory.openSession();
        Set<EntityType<?>> entities = session.getMetamodel().getEntities();
        session.beginTransaction();
        for (EntityType<?> entityType : entities) {
            String tableName = entityType.getJavaType().getAnnotation(Table.class).name();
            String sql = String.format("TRUNCATE TABLE %s CASCADE",tableName);
            Query query = session.createNativeQuery(sql);
            query.executeUpdate();
        }
        session.getTransaction().commit();
    }
}
