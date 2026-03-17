package jakarta.tutorial.timersession.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TimerController {

    private final TimerManager timerManager;

    public TimerController(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    @GetMapping("/")
    public String timerPage(Model model) {
        model.addAttribute(
                "lastProgrammaticTimeout",
                timerManager.getLastProgrammaticTimeout());

        model.addAttribute(
                "lastAutomaticTimeout",
                timerManager.getLastAutomaticTimeout());
        return "timer-client";
    }

    @PostMapping("/set")
    public String setTimer() {
        timerManager.setTimer();
        return "redirect:/";
    }
}