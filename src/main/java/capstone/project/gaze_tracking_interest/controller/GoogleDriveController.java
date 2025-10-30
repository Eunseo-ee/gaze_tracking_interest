package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.config.GoogleDriveUtil;
import capstone.project.gaze_tracking_interest.dto.DriveFileDto;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class GoogleDriveController {

    // ✅ Google Drive의 “capstone-design” 폴더 ID (고정)
    private static final String CAPSTONE_FOLDER_ID = "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz";

    /**
     * ✅ 특정 폴더 내 파일 목록 조회
     * @param folderId 폴더 ID
     * @param mimeType MIME 타입 필터 (ex: video/mp4, text/csv 등)
     */
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(
            @RequestParam String folderId,
            @RequestParam(defaultValue = "") String mimeType
    ) {
        try {
            List<File> files = GoogleDriveUtil.listFilesInFolder(folderId, mimeType);

            List<DriveFileDto> result = files.stream()
                    .map(file -> new DriveFileDto(
                            file.getId(),
                            file.getName(),
                            file.getMimeType(),
                            file.getWebViewLink(),
                            file.getWebContentLink()
                    ))
                    .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Google Drive 폴더 내 mp4 파일 목록 조회
     */
    @GetMapping("/mp4-list")
    public ResponseEntity<?> getMp4Files() {
        try {
            List<File> files = GoogleDriveUtil.listFilesInFolder(CAPSTONE_FOLDER_ID, "video/mp4");

            List<Map<String, String>> videoList = files.stream().map(file -> {
                Map<String, String> map = new HashMap<>();
                map.put("name", file.getName());
                map.put("url", file.getWebContentLink());  // 다운로드 링크
                map.put("webViewLink", file.getWebViewLink());  // 미리보기 링크
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(videoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Google Drive 폴더 내 CSV 파일 이름만 조회
     */
    @GetMapping("/csv-list")
    public ResponseEntity<?> getCsvFileNames() {
        try {
            List<File> files = GoogleDriveUtil.listFilesInFolder(CAPSTONE_FOLDER_ID, null);

            // ✅ .csv 확장자로 필터링
            List<String> fileNames = files.stream()
                .map(File::getName)
                .filter(name -> name.toLowerCase().endsWith(".csv"))
                .collect(Collectors.toList());

            return ResponseEntity.ok(fileNames);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * ✅ CSV 파일의 실제 내용을 가져오기
     * @param name 파일 이름
     */
    @GetMapping("/file")
    public ResponseEntity<?> getCsvContent(@RequestParam String name) {
        try {
            String decodedName = java.net.URLDecoder.decode(name, java.nio.charset.StandardCharsets.UTF_8);

            // ✅ MIME 필터 제거 → 모든 파일 불러오기
            List<File> files = GoogleDriveUtil.listFilesInFolder(CAPSTONE_FOLDER_ID, null);

            File target = files.stream()
                    .filter(f -> f.getName().equals(decodedName))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                return ResponseEntity.status(404).body(Map.of("error", "파일을 찾을 수 없습니다: " + decodedName));
            }

            String content = GoogleDriveUtil.downloadFileContent(target.getId());
            return ResponseEntity.ok(content);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}