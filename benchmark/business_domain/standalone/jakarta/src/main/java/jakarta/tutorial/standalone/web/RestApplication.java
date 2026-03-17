package jakarta.tutorial.standalone.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class RestApplication extends Application {
    // No additional configuration needed
    // JAX-RS will auto-discover resource classes
}
