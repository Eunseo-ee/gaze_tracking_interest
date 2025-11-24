package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.config.GoogleDriveUtil;
import capstone.project.gaze_tracking_interest.entity.Store;
import capstone.project.gaze_tracking_interest.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.api.services.drive.model.File;

@Controller
@RequiredArgsConstructor
public class MainRankingsController {

    private final StoreRepository storeRepository;

    @GetMapping("/")
    public String home() {
        return "main_rankings";  // 기본 메인 페이지
    }

    @GetMapping("/store/B06/promotion")
    public String promotion(Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", "B06");
        return "promotion";
    }

    @GetMapping({"/store/B06/rankings", "/api/store/B06"})
    public String rankings(Model model) {
        model.addAttribute("activeTab", "rankings");
        model.addAttribute("storeCode", "B06");

        try {
            // ✅ Drive 폴더 내 전체 파일 가져오기
            List<com.google.api.services.drive.model.File> files =
                    GoogleDriveUtil.listFilesInFolder("1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz", null);

            System.out.println("📂 [Drive 파일 목록]");
            for (var f : files) {
                System.out.println(" - " + f.getName() + " (" + f.getMimeType() + ")");
            }

            // ✅ CSV 파일만 필터링
            List<File> csvFiles = files.stream()
                .filter(f -> {
                    String name = f.getName().toLowerCase();
                    return name.startsWith("gaze-tracking") && name.endsWith(".csv");
                })
                .toList();


            if (csvFiles.isEmpty()) {
                System.out.println("⚠️ CSV 파일이 없습니다.");
                model.addAttribute("csvUrl", null);
                model.addAttribute("csvData", null);
                model.addAttribute("categories", null);
                return "main_rankings";
            }

            // ✅ 최신 CSV 파일 선택
            com.google.api.services.drive.model.File csvFile = csvFiles.stream()
                    .max(Comparator.comparing(f -> f.getModifiedTime().getValue()))
                    .get();

            System.out.println("✅ 최신 CSV 파일: " + csvFile.getName());

            model.addAttribute("csvUrl", csvFile.getWebViewLink());

            // ✅ Drive에서 CSV 파일 내용 다운로드
            String csvContent = GoogleDriveUtil.downloadFileContent(csvFile.getId());
            List<List<String>> csvData = new ArrayList<>();
            Set<String> categorySet = new TreeSet<>();

            try (Scanner scanner = new Scanner(csvContent)) {

                boolean isFirstLine = true;

                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine();

                    // 1) 공백 제거(split + trim)
                    String[] cols = Arrays.stream(line.split(","))
                                        .map(String::trim)
                                        .toArray(String[]::new);

                    // index = "-" 이면 건너뜀
                    if (cols.length > 0 && cols[0].equals("-")) {
                        continue;
                    }

                    List<String> row = new ArrayList<>(Arrays.asList(cols));

                    if (row.size() >= 4) {
                        row.remove(0); // index 제거
                    }

                    // 가격에 원 자 없앰
                    if (row.size() > 2) {
                        String price = row.get(2).trim().replaceAll("[^0-9]", "");
                        row.set(2, price);
                    }

                    // 3) 최소 컬럼 길이 보장 (상품명, 카테고리, 가격)
                    while (row.size() < 3) {
                        row.add("");
                    }

                    // 4) 카테고리 수집
                    if (row.size() > 2) {
                        String category = row.get(1).trim();
                        if (!category.isEmpty()) {
                            categorySet.add(category);
                        }
                    }

                    csvData.add(row);
                    isFirstLine = false;
                }
            }


            // ✅ model에 데이터 전달
            model.addAttribute("csvData", csvData);
            model.addAttribute("categories", categorySet);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("csvUrl", null);
            model.addAttribute("csvData", null);
            model.addAttribute("categories", null);
        }

        return "main_rankings";
    }


    @GetMapping("/store/B06/owner")
    public String owner(Model model) {
        Store store = storeRepository.findByStoreCode("B06")
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + "B06"));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", "B06");
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "owner";  //
    }

    @GetMapping("/store/B06/owner_dashboard")
    public String dashboard(Model model) {
        Store store = storeRepository.findByStoreCode("B06")
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + "B06"));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", "B06");
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard";  //
    }

    @GetMapping("/store/B06/dashboard_video")
    public String dashboard_video(Model model) {
        Store store = storeRepository.findByStoreCode("B06")
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + "B06"));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", "B06");
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        // 영상 파일 목록 가져오기
       try {
            List<File> files = GoogleDriveUtil.listFilesInFolder(
                    "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz",
                    null
            );
        System.out.println("🎥 [Drive Video Files]");
            List<String> videoList = files.stream()
                    .filter(f -> {
                        String name = f.getName().toLowerCase().replaceAll("\\s", "");
                        return name.matches(".*\\.(mp4|avi|mov|mkv)$");
                    })
                    .map(File::getWebContentLink)
                    .toList();

            model.addAttribute("videoList", videoList);

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            model.addAttribute("videoList", Collections.emptyList());
        }
        System.out.println("🎥 [Drive Video Files]");
        return "dashboard_video";
    }

    @GetMapping("/store/B06/dashboard_comparison")
    public String dashboard_comparison(Model model) {
        Store store = storeRepository.findByStoreCode("B06")
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + "B06"));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", "B06");
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard_comparison";  //
    }

    @GetMapping("/store/B06/dashboard_upload")
    public String dashboard_upload(Model model) {
        Store store = storeRepository.findByStoreCode("B06")
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + "B06"));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", "B06");
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard_upload";  // ✅ templates/dashboard_upload.html 로 연결
    }

}

