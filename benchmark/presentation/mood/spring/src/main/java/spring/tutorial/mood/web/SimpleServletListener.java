package spring.tutorial.mood.web;

import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleServletListener implements ServletContextListener, ServletContextAttributeListener {
    private static final Logger log = LoggerFactory.getLogger(SimpleServletListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Context destroyed");
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        log.info("Attribute added: {}={}", event.getName(), event.getValue());
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        log.info("Attribute removed: {}", event.getName());
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        log.info("Attribute replaced: {}", event.getName());
    }
}
