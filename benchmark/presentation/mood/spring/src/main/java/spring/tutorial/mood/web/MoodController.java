package spring.tutorial.mood.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MoodController {

    @GetMapping(value = "/report", produces = MediaType.TEXT_HTML_VALUE)
    public String getReport(HttpServletRequest request,
                            @RequestParam(required = false, defaultValue = "") String name) {
        String mood = (String) request.getAttribute("mood");

        return """
               <!doctype html>
               <html lang="en">
                 <head><meta charset="utf-8"><title>Servlet MoodServlet</title></head>
                 <body>
                   <h1>Mood report</h1>
                   <p>Duke's mood is: %s</p>
                   <img src="/images/duke.waving.gif" alt="duke waving">
                 </body>
               </html>
               """.formatted(mood);
    }

    @PostMapping(value = "/report", produces = MediaType.TEXT_HTML_VALUE)
    public String postReport(HttpServletRequest request,
                             @RequestParam(required = false, defaultValue = "") String name) {
        // Reuse the GET logic (your original servlet had doPost→processRequest)
        return getReport(request, name);
    }
}
