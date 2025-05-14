package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.entity.Store;
import capstone.project.gaze_tracking_interest.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MainRankingsController {

    private final StoreRepository storeRepository;

    @GetMapping("/")
    public String home() {
        return "main_rankings";  // 기본 메인 페이지
    }

    @GetMapping("/store/{storeCode}/promotion")
    public String promotion(@PathVariable String storeCode, Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);  //
        return "promotion";
    }

    @GetMapping("/store/{storeCode}/rankings")
    public String rankings(@PathVariable String storeCode, Model model) {
        model.addAttribute("activeTab", "rankings");
        model.addAttribute("storeCode", storeCode);  //
        return "main_rankings";
    }

    @GetMapping("/store/{storeCode}/owner")
    public String owner(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "owner";  //
    }

    @GetMapping("/store/{storeCode}/owner_dashboard")
    public String dashboard(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard";  //
    }

    @GetMapping("/store/{storeCode}/dashboard_video")
    public String dashboard_video(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard_video";  //
    }

    @GetMapping("/store/{storeCode}/dashboard_comparison")
    public String dashboard_comparison(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard_comparison";  //
    }
}

