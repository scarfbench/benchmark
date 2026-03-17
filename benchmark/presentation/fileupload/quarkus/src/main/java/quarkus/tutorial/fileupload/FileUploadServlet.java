package quarkus.tutorial.fileupload;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;
import quarkus.tutorial.fileupload.FileUploadForm;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/upload")
public class FileUploadServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getCanonicalName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response uploadFile(@MultipartForm FileUploadForm form) {

        String path = form.destination;
        String fileName = form.filename != null ? form.filename : "uploaded_file";
        byte[] fileContent = form.file;

        if (path == null || path.trim().isEmpty()) {
            return Response.ok("Please specify a destination directory for the file upload.").build();
        }

        try (OutputStream out = new FileOutputStream(new File(path + File.separator + fileName))) {
            out.write(fileContent);
            LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
            return Response.ok("New file " + fileName + " created at " + path).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Upload failed: " + e.getMessage()).build();
        }
    }
}
