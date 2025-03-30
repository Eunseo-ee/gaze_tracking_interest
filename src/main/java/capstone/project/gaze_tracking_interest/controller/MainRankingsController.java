package capstone.project.gaze_tracking_interest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainRankingsController {
    @GetMapping("/")
    public String home() {
        return "main_rankings";  // templates/main_rankings.html 호출
    }

    @GetMapping("/promotion")
    public String promotion(Model model) {
        model.addAttribute("activeTab", "promotion");
        return "promotion";
    }

    @GetMapping("/rankings")
    public String rankings(Model model) {
        model.addAttribute("activeTab", "rankings");
        return "main_rankings";
    }

    @GetMapping("/owner")
    public String owner(Model model) {
        model.addAttribute("activeTab", "owner");
        return "owner";
    }
}
