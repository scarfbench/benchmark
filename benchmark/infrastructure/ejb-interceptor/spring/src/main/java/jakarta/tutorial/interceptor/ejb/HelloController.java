package jakarta.tutorial.interceptor.ejb;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("helloForm")) {
            model.addAttribute("helloForm", new HelloForm());
        }
        return "index";
    }

    @PostMapping("/response")
    public String response(@ModelAttribute("helloForm") HelloForm helloForm, Model model) {
        model.addAttribute("name", helloForm.getName());
        return "response";
    }
}
