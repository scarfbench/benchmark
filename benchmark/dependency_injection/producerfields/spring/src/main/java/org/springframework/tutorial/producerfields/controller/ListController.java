package org.springframework.tutorial.producerfields.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.tutorial.producerfields.dto.ToDoForm;
import org.springframework.tutorial.producerfields.entity.ToDo;
import org.springframework.tutorial.producerfields.service.RequestService;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
public class ListController {

    @Autowired
    private RequestService requestService;

    @GetMapping
    public String showForm(Model model) {
        if (!model.containsAttribute("toDoForm")) {
            model.addAttribute("toDoForm", new ToDoForm());
        }
        return "index";
    }

    @PostMapping("/create")
    public String createToDo(@ModelAttribute("toDoForm") @Valid ToDoForm toDoForm,
            BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "index";
        }

        requestService.createToDo(toDoForm.getInputString());

        redirectAttributes.addFlashAttribute("toDoForm", toDoForm);

        return "redirect:/";
    }

    @GetMapping("/todolist")
    public String showToDoList(Model model) {
        List<ToDo> toDos = requestService.getToDos();
        model.addAttribute("toDos", toDos);
        return "todolist";
    }

}
