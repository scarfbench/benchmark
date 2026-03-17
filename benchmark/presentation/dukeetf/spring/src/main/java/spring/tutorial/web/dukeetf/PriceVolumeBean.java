package spring.tutorial.web.dukeetf;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PriceVolumeBean {
    private Random random;
    private DukeETFServlet servlet;
    private volatile double price = 100.0;
    private volatile int volume = 300000;
    private static final Logger logger = Logger.getLogger("PriceVolumeBean");

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "Initializing PriceVolumeBean.");
        random = new Random();
        servlet = null;
    }

    public void registerServlet(DukeETFServlet servlet) {
        this.servlet = servlet;
    }

    @Scheduled(fixedDelay = 1000)
    public void timeout() {
        price += 1.0 * (random.nextInt(100) - 50) / 100.0;
        volume += random.nextInt(5000) - 2500;
        if (servlet != null) {
            servlet.send(price, volume);
        }
    }
}
