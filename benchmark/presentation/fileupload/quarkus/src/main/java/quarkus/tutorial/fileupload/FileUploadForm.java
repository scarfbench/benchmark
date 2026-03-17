package quarkus.tutorial.fileupload;

import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.reactive.PartType;

public class FileUploadForm {

    @FormParam("destination")
    public String destination;

    @FormParam("file")
    @PartType("application/octet-stream")
    public byte[] file;

    @FormParam("filename")
    @PartType("text/plain")
    public String filename;
}
