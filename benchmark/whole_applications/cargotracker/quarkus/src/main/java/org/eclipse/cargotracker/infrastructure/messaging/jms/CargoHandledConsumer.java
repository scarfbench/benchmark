package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * Consumes JMS messages and delegates notification of misdirected cargo to the tracking service.
 *
 * <p>
 * This is a programmatic hook into the JMS infrastructure to make cargo inspection message-driven.
 */
@ApplicationScoped
public class CargoHandledConsumer implements Runnable {

  @Inject
  private Logger logger;

  @Inject
  private ConnectionFactory connectionFactory;

  @Inject
  private CargoInspectionService cargoInspectionService;

  @ConfigProperty(name = "app.jms.CargoHandledQueue", defaultValue = "CargoHandledQueue")
  private String cargoHandledQueue;

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
      JMSConsumer consumer = context.createConsumer(context.createQueue(cargoHandledQueue));
      while (true) {
        Message message = consumer.receive();
        TextMessage textMessage = (TextMessage) message;
        String trackingIdString = textMessage.getText();

        cargoInspectionService.inspectCargo(new TrackingId(trackingIdString));
      }
    } catch (JMSException e) {
      logger.log(Level.SEVERE, "Error procesing JMS message", e);
    }
  }

}
