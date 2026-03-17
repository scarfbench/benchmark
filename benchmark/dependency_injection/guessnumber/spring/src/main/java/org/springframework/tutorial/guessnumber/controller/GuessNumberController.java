package org.springframework.tutorial.guessnumber.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.tutorial.guessnumber.config.MaxNumber;
import org.springframework.tutorial.guessnumber.config.Random;
import org.springframework.tutorial.guessnumber.dto.UserNumberBean;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes("userNumberBean")
public class GuessNumberController {

    @Autowired
    @Random
    private ObjectProvider<Integer> randomIntProvider;
    @Autowired
    @MaxNumber
    private Integer maxNumber;

    @ModelAttribute("userNumberBean")
    public UserNumberBean getUserNumberBean() {
        return new UserNumberBean(randomIntProvider.getObject(), maxNumber);
    }

    @GetMapping("/")
    public String showForm(@ModelAttribute("userNumberBean") UserNumberBean bean) {
        return "index";
    }

    @PostMapping("/guess")
    public String processGuess(
            @ModelAttribute("userNumberBean") UserNumberBean bean,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "index";
        }
        bean.validateNumberRange(result);
        if (result.hasErrors()) {
            return "index";
        }

        bean.check();

        if (bean.getNumber() == bean.getUserNumber()) {
            // a hack to display the message
            result.rejectValue(null, "correct", "Correct!");
            return "index";
        }

        return "redirect:/";
    }

    @PostMapping("/reset")
    public String reset(@ModelAttribute("userNumberBean") UserNumberBean bean) {
        bean.reset(randomIntProvider.getObject(), maxNumber);
        return "redirect:/";
    }
}
