package org.example.realworldapi;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

@ApplicationScoped
public class DatabaseIntegrationTest {

  @Inject EntityManager em;

  private Set<String> entities = new HashSet<>();

  @PostConstruct
  private void init() {
    Configuration configuration = new Configuration();
    configEntityClasses(configuration);
  }

  private void configEntityClasses(Configuration configuration) {
    Reflections reflections = new Reflections("org.example.realworldapi");
    reflections
        .getTypesAnnotatedWith(Entity.class)
        .forEach(
            entity -> {
              String tableName = entity.getAnnotation(Table.class).name();
              entities.add(tableName);
              configuration.addAnnotatedClass(entity);
            });
  }

  @Transactional
  public void truncate() {
    entities.forEach(
        tableName ->
            em.createNativeQuery("TRUNCATE TABLE " + tableName + " CASCADE;").executeUpdate());
  }
}
