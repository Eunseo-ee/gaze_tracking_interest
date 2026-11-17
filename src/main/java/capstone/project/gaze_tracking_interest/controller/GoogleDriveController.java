package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.datasource.DataFile;
import capstone.project.gaze_tracking_interest.datasource.DataSourceService;
import capstone.project.gaze_tracking_interest.dto.DriveFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/** 경로는 기존 프론트 호환을 위해 /api/drive 유지. 실제 데이터 소스(Drive/로컬)는 DataSourceService가 결정한다. */
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class GoogleDriveController {

    private final DataSourceService dataSource;

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
            List<DriveFileDto> result = dataSource.listFiles(folderId, mimeType).stream()
                    .map(file -> new DriveFileDto(
                            file.id(),
                            file.name(),
                            file.mimeType(),
                            file.webViewLink(),
                            file.webContentLink()
                    ))
                    .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * ✅ 폴더 내 mp4 파일 목록 조회 (로컬 모드에서는 sample-data에 mp4가 없으면 빈 목록)
     */
    @GetMapping("/mp4-list")
    public ResponseEntity<?> getMp4Files() {
        try {
            List<Map<String, String>> videoList = dataSource.listFiles().stream()
                    .filter(file -> file.name().toLowerCase().endsWith(".mp4"))
                    .map(file -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("name", file.name());
                        map.put("url", file.webContentLink());      // 다운로드 링크
                        map.put("webViewLink", file.webViewLink()); // 미리보기 링크
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(videoList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * ✅ 로컬 모드 영상 스트리밍 (Range 요청은 Spring이 Resource에 대해 자동 처리)
     */
    @GetMapping("/video")
    public ResponseEntity<Resource> getVideo(@RequestParam String name) {
        try {
            return dataSource.openVideo(name)
                    .map(res -> ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("video/mp4"))
                            .body(res))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ✅ CSV 파일 이름만 조회
     */
    @GetMapping("/csv-list")
    public ResponseEntity<?> getCsvFileNames() {
        try {
            return ResponseEntity.ok(dataSource.listCsvNames());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", String.valueOf(e.getMessage())));
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

            Optional<DataFile> target = dataSource.findByName(decodedName);
            if (target.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "파일을 찾을 수 없습니다: " + decodedName));
            }

            return ResponseEntity.ok(dataSource.readContent(target.get()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

}
