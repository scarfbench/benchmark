package org.springframework.tutorial.simplegreeting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.tutorial.simplegreeting.Informal;
import org.springframework.tutorial.simplegreeting.dto.PrinterForm;
import org.springframework.tutorial.simplegreeting.service.Greeting;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PrinterController {

    private final Greeting greeting;

    public PrinterController(@Informal Greeting greeting) {
        this.greeting = greeting;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        if (!model.containsAttribute("printerForm")) {
            model.addAttribute("printerForm", new PrinterForm());
        }
        return "index";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("printerForm") PrinterForm printerForm,
            Model model,
            RedirectAttributes redirectAttributes) {
        printerForm.setSalutation(greeting.greet(printerForm.getName()));
        redirectAttributes.addFlashAttribute("printerForm", printerForm);

        return "redirect:/";
    }
}
