package capstone.project.gaze_tracking_interest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/store/B06/promotion")
public class PromotionController {

    private final String storeCode = "B06"; // 🔥 고정

    @GetMapping("/promotion_detail1")
    public String detail1(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode); // ⭐ 추가
        return "promotion_detail1";
    }

    @GetMapping("/promotion_detail2")
    public String detail2(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion_detail2";
    }

    @GetMapping("/promotion_detail3")
    public String detail3(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion_detail3";
    }

    @GetMapping("/promotion_detail4")
    public String detail4(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion_detail4";
    }

    @GetMapping("/promotion_detail5")
    public String detail5(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion_detail5";
    }

    @GetMapping("/promotion_detail6")
    public String detail6(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion_detail6";
    }
}
