package quarkus.tutorial.web.dukeetf;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import quarkus.tutorial.web.dukeetf.PriceVolumeService;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/dukeetf")
public class DukeETFServlet {

    private static final Logger logger = Logger.getLogger("DukeETFResource");

    @Inject
    PriceVolumeService service;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public CompletableFuture<Response> getETFData() {
        CompletableFuture<Response> future = new CompletableFuture<>();

        service.register(future);

        logger.log(Level.INFO, "Connection open (queued).");

        return future;
    }
}
