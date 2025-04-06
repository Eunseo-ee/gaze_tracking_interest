package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.dto.LoginRequest;
import capstone.project.gaze_tracking_interest.dto.PasswordRequest;
import capstone.project.gaze_tracking_interest.entity.Store;
import capstone.project.gaze_tracking_interest.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")

public class StoreAuthController {

    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ 사업자 인증 확인
    @GetMapping("/verify-biznum")
    public ResponseEntity<?> verifyBizNum(
            @RequestParam String storeCode,
            @RequestParam String inputBizNum
    ) {
        Optional<Store> storeOpt = storeRepository.findByStoreCode(storeCode);
        if (storeOpt.isPresent()) {
            String registeredBizNum = storeOpt.get().getBusinessNumber();
            boolean match = registeredBizNum.equals(inputBizNum);

            return ResponseEntity.ok(Collections.singletonMap("verified", match));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "가게를 찾을 수 없습니다."));
    }


    // ✅ 비밀번호 설정
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody PasswordRequest req) {
        Optional<Store> storeOpt = storeRepository.findByStoreCode(req.getStoreCode());
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            store.setVerified(true);
            storeRepository.save(store);
            return ResponseEntity.ok("비밀번호 저장 완료");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("가게를 찾을 수 없습니다.");
    }

    // ✅ 비밀번호 로그인 확인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Store> storeOpt = storeRepository.findByStoreCode(req.getStoreCode());
        if (storeOpt.isPresent()) {
            boolean match = passwordEncoder.matches(req.getPassword(), storeOpt.get().getPasswordHash());
            if (match) return ResponseEntity.ok("로그인 성공");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
    }
}

