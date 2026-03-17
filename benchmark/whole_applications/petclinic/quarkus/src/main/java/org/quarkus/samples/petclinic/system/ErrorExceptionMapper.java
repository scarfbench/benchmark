package org.quarkus.samples.petclinic.system;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ErrorExceptionMapper {

    private static final Logger LOG = Logger.getLogger(ErrorExceptionMapper.class);
    public static final String ERROR_HEADER = "x-error";

    @Inject
    TemplatesLocale templates;

    @ServerExceptionMapper
    public Response map(Exception exception) {
        LOG.error("Internal application error", exception);
        return Response.ok(templates.error(exception.getMessage())).header(ERROR_HEADER, true).build();
    }

}
