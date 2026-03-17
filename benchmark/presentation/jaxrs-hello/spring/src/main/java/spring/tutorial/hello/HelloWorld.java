package spring.tutorial.hello;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("helloWorld")
@RequestMapping(path = "helloworld")
public class HelloWorld {

    public HelloWorld() {
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String getHtml() {
        return "<html lang=\"en\"><body><h1>Hello, World!!</h1></body></html>";
    }

    @PutMapping(consumes = MediaType.TEXT_HTML_VALUE)
    public void putHtml(String content) {
    }
}