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
public class OwnerPageController {

    private final StoreRepository storeRepository;

    @GetMapping("/store/{storeCode}/owner")
    public String ownerPage(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "owner";  // 👉 templates/owner.html 렌더링됨
    }
}

