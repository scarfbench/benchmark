package quarkus.tutorial.mood;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.StringWriter;
import java.io.PrintWriter;



@Path("/report")
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
public class MoodServlet { 

    @GET
    public String get(@Context ContainerRequestContext ctx, @Context UriInfo uriInfo) {
        return render(ctx, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@FormParam("override") String override,
                       @Context ContainerRequestContext ctx,
                       @Context UriInfo uriInfo) {
        if (override != null && !override.isBlank()) {
            ctx.setProperty("mood", override.trim());
        }
        return render(ctx, uriInfo);
    }

    private String render(ContainerRequestContext ctx, UriInfo uriInfo) {
        final String contextPath = uriInfo.getBaseUri().getPath();
        String mood = (String) ctx.getProperty("mood");
        if (mood == null || mood.isBlank()) mood = "neutral";

        StringWriter sw = new StringWriter();
        try (PrintWriter out = new PrintWriter(sw)) {
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>Servlet MoodServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MoodServlet at " + contextPath + "</h1>");
            out.println("<p>Duke's mood is: " + mood + "</p>");
            switch (mood) {
                case "sleepy":
                    out.println("<img src=\"/images/duke.snooze.gif\" alt=\"Duke sleeping\"/><br/>");
                    break;
                case "alert":
                    out.println("<img src=\"/images/duke.waving.gif\" alt=\"Duke waving\"/><br/>");
                    break;
                case "hungry":
                    out.println("<img src=\"/images/duke.cookies.gif\" alt=\"Duke with cookies\"/><br/>");
                    break;
                case "lethargic":
                    out.println("<img src=\"/images/duke.handsOnHips.gif\" alt=\"Duke with hands on hips\"/><br/>");
                    break;
                case "thoughtful":
                    out.println("<img src=\"/images/duke.pensive.gif\" alt=\"Duke thinking\"/><br/>");
                    break;
                default:
                    out.println("<img src=\"/images/duke.thumbsup.gif\" alt=\"Duke with thumbs-up gesture\"/><br/>");
                    break;
            }
            out.println("</body>");
            out.println("</html>");
        }
        return sw.toString();
    }
}
