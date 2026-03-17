package spring.tutorial.fileupload;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FileUploadController {

    private static final Logger LOGGER =
            Logger.getLogger(FileUploadController.class.getCanonicalName());

    @PostMapping(path = "/upload",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> upload(@RequestParam("destination") String destination,
                                         @RequestParam("file") MultipartFile file) {
        if (!StringUtils.hasText(destination)) {
            return ResponseEntity.ok("Destination must be provided");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.ok("You did not specify a file to upload.");
        }

        try {
            Path destDir = Paths.get(destination).normalize().toAbsolutePath();
            Files.createDirectories(destDir);

            String original = file.getOriginalFilename();
            String fileName = (original == null || original.isBlank())
                    ? "upload.bin"
                    : Paths.get(original).getFileName().toString();

            Path target = destDir.resolve(fileName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                    new Object[]{fileName, destDir});
            return ResponseEntity.ok("New file " + fileName + " created at " + destDir);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{ex.getMessage()});
            String msg = "You either did not specify a file to upload or are trying to upload a file to a protected or nonexistent location."
                    + "<br/> ERROR: " + ex.getMessage();
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(msg);
        }
    }

    @GetMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Servlet that uploads files to a user-defined destination");
    }
}
