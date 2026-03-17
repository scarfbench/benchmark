
package quarkus.tutorial.order.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import java.util.logging.Logger;
import quarkus.tutorial.order.service.OrderConfigService;

@ApplicationScoped
public class StartupInitializer {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Inject
    OrderConfigService orderConfigService;

    void onStart(@Observes StartupEvent ev) {
        logger.info("Manually triggering dataset initialization");
        orderConfigService.createData();
        logger.info("Manual dataset initialization completed");
    }
}
