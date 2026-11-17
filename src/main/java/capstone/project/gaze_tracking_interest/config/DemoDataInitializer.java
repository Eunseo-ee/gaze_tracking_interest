package capstone.project.gaze_tracking_interest.config;

import capstone.project.gaze_tracking_interest.entity.Store;
import capstone.project.gaze_tracking_interest.repository.StoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** 로컬 샘플 모드 첫 실행 시 stores가 비어 있으면 시연용 점주 1건을 만든다. 값은 모두 가짜. */
@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "local", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    static final String DEMO_STORE_CODE = "B06";
    static final String DEMO_BUSINESS_NUMBER = "000-00-00000";
    static final String DEMO_PASSWORD = "demo1234";

    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(StoreRepository storeRepository, PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (storeRepository.count() > 0) {
                return;
            }
            Store store = new Store();
            store.setStoreCode(DEMO_STORE_CODE);
            store.setStoreName("데모 편의점 (시연용)");
            store.setBusinessNumber(DEMO_BUSINESS_NUMBER);
            store.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            store.setVerified(true);
            storeRepository.save(store);
            System.out.println("✅ 데모 점주 계정을 생성했습니다. (매장 코드 " + DEMO_STORE_CODE + ")");
        } catch (Exception e) {
            System.out.println("⚠️ MongoDB에 연결할 수 없어 데모 점주를 만들지 못했습니다: " + e.getClass().getSimpleName());
        }
    }
}
