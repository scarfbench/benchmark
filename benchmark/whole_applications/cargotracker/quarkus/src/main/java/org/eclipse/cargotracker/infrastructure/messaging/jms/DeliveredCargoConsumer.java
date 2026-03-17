package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

@ApplicationScoped
public class DeliveredCargoConsumer implements Runnable {

  @Inject
  private Logger logger;

  @Inject
  private ConnectionFactory connectionFactory;

  @ConfigProperty(name = "app.jms.DeliveredCargoQueue", defaultValue = "DeliveredCargoQueue")
  private String deliveredCargoQueue;

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  public void onStart(@Observes StartupEvent ev) {
    scheduler.submit(this);
  }

  public void onStop(@Observes ShutdownEvent ev) {
    scheduler.shutdown();
  }

  @Override
  public void run() {
    try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
      JMSConsumer consumer = context.createConsumer(context.createQueue(deliveredCargoQueue));
      while (true) {
        Message message = consumer.receive();
        logger.log(
            Level.INFO, "Cargo with tracking ID {0} delivered.", message.getBody(String.class));
      }
    } catch (JMSException ex) {
      logger.log(Level.WARNING, "Error processing message.", ex);
    }
  }

}
