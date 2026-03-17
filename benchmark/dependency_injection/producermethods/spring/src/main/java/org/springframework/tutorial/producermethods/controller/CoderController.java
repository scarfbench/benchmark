package org.springframework.tutorial.producermethods.controller;

import org.springframework.stereotype.Controller;
import org.springframework.tutorial.producermethods.dto.CoderForm;
import org.springframework.tutorial.producermethods.service.Coder;
import org.springframework.tutorial.producermethods.service.CoderFactory;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

/**
 * Web controller that calls a Coder implementation to perform a transformation on an input string.
 */
@Controller
public class CoderController {

    private final CoderFactory coderFactory;

    public CoderController(CoderFactory coderFactory) {
        this.coderFactory = coderFactory;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        if (!model.containsAttribute("coderForm")) {
            model.addAttribute("coderForm", new CoderForm());
        }
        return "index";
    }

    /**
     * Encode the input string.
     *
     * @return the response page location
     */
    @PostMapping("/encode")
    public String encode(@Valid @ModelAttribute("coderForm") CoderForm coderForm,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "index";
        }

        Coder coderService = coderFactory.getCoder(coderForm.getCoderType());
        String encodedString =
                coderService.codeString(coderForm.getInputString(), coderForm.getTransVal());
        coderForm.setCodedString(encodedString);

        redirectAttributes.addFlashAttribute("coderForm", coderForm);

        return "redirect:/";
    }

    /**
     * Resets the values in the index page.
     */
    @PostMapping("/reset")
    public String reset(@ModelAttribute("coderForm") CoderForm coderForm, Model model,
            RedirectAttributes redirectAttributes) {
        CoderForm form = new CoderForm();
        form.setCoderType(coderForm.getCoderType());
        redirectAttributes.addFlashAttribute("coderForm", form);

        return "redirect:/";
    }
}
