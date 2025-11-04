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

    @GetMapping("/store/{storeCode}/promotion")
    public String promotion(@PathVariable String storeCode, Model model) {
        model.addAttribute("activeTab", "promotion");
        model.addAttribute("storeCode", storeCode);
        return "promotion";
    }

    @GetMapping({"/store/{storeCode}/rankings", "/api/store/{storeCode}"})
    public String rankings(@PathVariable String storeCode, Model model) {
        model.addAttribute("activeTab", "rankings");
        model.addAttribute("storeCode", storeCode);

        try {
            // ✅ Drive 폴더 내 전체 파일 가져오기
            List<com.google.api.services.drive.model.File> files =
                    GoogleDriveUtil.listFilesInFolder("1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz", null);

            System.out.println("📂 [Drive 파일 목록]");
            for (var f : files) {
                System.out.println(" - " + f.getName() + " (" + f.getMimeType() + ")");
            }

            // ✅ CSV 파일만 필터링
            List<com.google.api.services.drive.model.File> csvFiles = files.stream()
                    .filter(f -> f.getName().toLowerCase().endsWith(".csv"))
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
                    List<String> row = new ArrayList<>(Arrays.asList(line.split(",")));

                    if (!isFirstLine && row.size() > 3) {
                        row.remove(0); // index 제거
                        row.remove(1); // 바코드 제거
                    }

                    if (!isFirstLine && row.size() > 2) {
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

        // 영상 파일 목록 가져오기
        Path uploadPath = Paths.get("src/main/resources/static/uploads/");
        List<String> mp4List = new ArrayList<>();
        try (Stream<Path> paths = Files.list(uploadPath)) {
            mp4List = paths
                    .filter(p -> p.toString().endsWith(".mp4"))
                    .map(p -> "/uploads/" + p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("mp4List", mp4List); // 뷰로 전달

        return "dashboard_video";  //
    }

    @GetMapping("/store/{storeCode}/dashboard_comparison")
    public String dashboard_comparison(@PathVariable String storeCode, Model model) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

        model.addAttribute("activeTab", "owner");
        model.addAttribute("storeCode", storeCode);
        model.addAttribute("storeName", store.getStoreName());
        model.addAttribute("businessNumber", store.getBusinessNumber());

        return "dashboard_comparison";  //
    }

    @GetMapping("/store/{storeCode}/dashboard_upload")
public String dashboard_upload(@PathVariable String storeCode, Model model) {
    Store store = storeRepository.findByStoreCode(storeCode)
            .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다: " + storeCode));

    model.addAttribute("activeTab", "owner");
    model.addAttribute("storeCode", storeCode);
    model.addAttribute("storeName", store.getStoreName());
    model.addAttribute("businessNumber", store.getBusinessNumber());

    return "dashboard_upload";  // ✅ templates/dashboard_upload.html 로 연결
}

}

