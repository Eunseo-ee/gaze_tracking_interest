package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.entity.Store;
import capstone.project.gaze_tracking_interest.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StorePageController {

    private final StoreRepository storeRepository;

    @GetMapping("/{storeCode}")
    public String storeHomePage(@PathVariable String storeCode, Model model) {
        // ✅ storeCode에 해당하는 가게 정보 조회
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 storeCode의 가게가 존재하지 않습니다: " + storeCode));

        // ✅ 템플릿으로 정보 전달
        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        System.out.println("storeCode: " + storeCode);
        System.out.println("storeName: " + store.getStoreName());
        System.out.println("businessNumber: " + store.getBusinessNumber());

        return "main_rankings"; // → templates/main_rankings.html
    }
}
