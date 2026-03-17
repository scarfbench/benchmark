package org.example.realworldapi;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.reflections.Reflections;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseIT extends AppDeploymentConfig {

    public static EntityManagerFactory entityManagerFactory;
    public static EntityManager entityManager;
    private static DataSource dataSource;
    private static Set<String> entities;

    @BeforeAll
    static void init() {
        entities = new HashSet<>();
        dataSource = dataSource();
        entityManagerFactory = sessionFactory();
        entityManager = entityManagerFactory.createEntityManager();
    }

    private static SessionFactory sessionFactory() {
        ServiceRegistry serviceRegistry = null;
        SessionFactory sessionFactory = null;
        try {
            Configuration configuration = configuration();
            StandardServiceRegistryBuilder standardServiceRegistryBuilder =
                    new StandardServiceRegistryBuilder();
            standardServiceRegistryBuilder.applySettings(configuration.getProperties());
            serviceRegistry = standardServiceRegistryBuilder.build();
            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception ex) {
            ex.printStackTrace();
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
        return sessionFactory;
    }

    private static Configuration configuration() {
        Configuration configuration = new Configuration();
        configuration.setProperties(properties());
        configEntityClasses(configuration, "org.example.realworldapi");
        return configuration;
    }

    private static Properties properties() {
        Properties properties = new Properties();
        properties.put(Environment.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
        properties.put(Environment.HBM2DDL_AUTO, "none");
        properties.put(Environment.JAKARTA_JTA_DATASOURCE, dataSource);
        properties.put(Environment.JAKARTA_NON_JTA_DATASOURCE, dataSource);
        return properties;
    }

    public void clear() {
        transaction(
                () ->
                        entities.forEach(
                                tableName ->
                                        entityManager
                                                .createNativeQuery(
                                                        "TRUNCATE TABLE "
                                                                + tableName
                                                                + " CASCADE;")
                                                .executeUpdate()));
    }

    private static DataSource dataSource() {
        PGSimpleDataSource jdbcDataSource = new PGSimpleDataSource();
        jdbcDataSource.setUrl(postgres.getJdbcUrl());
        jdbcDataSource.setUser("postgres_user");
        jdbcDataSource.setPassword("S3cret");
        return jdbcDataSource;
    }

    private static void configEntityClasses(Configuration configuration, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        reflections
                .getTypesAnnotatedWith(Entity.class)
                .forEach(
                        entity -> {
                            String tableName = entity.getAnnotation(Table.class).name();
                            entities.add(tableName);
                            configuration.addAnnotatedClass(entity);
                        });
    }

    public void transaction(Runnable command) {
        entityManager.getTransaction().begin();
        entityManager.flush();
        entityManager.clear();
        command.run();
        entityManager.getTransaction().commit();
    }

    public <T> T transaction(TransactionRunnable<T> command) {
        AtomicReference<T> atomicReference = new AtomicReference<>();
        transaction(() -> atomicReference.set(command.run()));
        return atomicReference.get();
    }

    public interface TransactionRunnable<T> {
        T run();
    }
}
