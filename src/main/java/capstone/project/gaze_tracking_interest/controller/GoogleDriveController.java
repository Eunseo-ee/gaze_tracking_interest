package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.config.GoogleDriveUtil;
import capstone.project.gaze_tracking_interest.dto.DriveFileDto;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class GoogleDriveController {

    @GetMapping("/files")
    public ResponseEntity<List<DriveFileDto>> listFiles(
            @RequestParam String folderId,
            @RequestParam(defaultValue = "") String mimeType
    ) throws Exception {
        List<File> files = GoogleDriveUtil.listFilesInFolder(folderId, mimeType);

        List<DriveFileDto> result = files.stream().map(file ->
                new DriveFileDto(file.getId(), file.getName(), file.getMimeType(), file.getWebViewLink(), file.getWebContentLink())
        ).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/mp4-list")
    public ResponseEntity<List<Map<String, String>>> getMp4Files() throws Exception {
        String folderId = "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz"; // 📁 capstone-design 폴더 ID
        List<File> files = GoogleDriveUtil.listFilesInFolder(folderId, "video/mp4");

        // 🎯 필요한 정보만 추출해서 프론트에 보냄
        List<Map<String, String>> videoList = files.stream().map(file -> {
            Map<String, String> map = new HashMap<>();
            map.put("name", file.getName());
            map.put("url", file.getWebContentLink());
            map.put("webViewLink", file.getWebViewLink()); // 추가
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(videoList);
    }

    @GetMapping("/csv-list")
    public ResponseEntity<List<String>> getCsvFileNames() throws Exception {
        String folderId = "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz"; // 동일 폴더 사용
        List<File> files = GoogleDriveUtil.listFilesInFolder(folderId, "text/csv");

        List<String> fileNames = files.stream()
                .map(File::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileNames);
    }

    @GetMapping("/file")
    public ResponseEntity<String> getCsvContent(@RequestParam String name) throws Exception {
        String folderId = "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz";

        // ✅ 인코딩된 이름 디코딩 처리
        String decodedName = java.net.URLDecoder.decode(name, java.nio.charset.StandardCharsets.UTF_8);

        List<File> files = GoogleDriveUtil.listFilesInFolder(folderId, "text/csv");

        File target = files.stream()
                .filter(f -> f.getName().equals(decodedName)) // 디코딩된 이름과 일치 비교
                .findFirst()
                .orElse(null);

        if (target == null) {
            return ResponseEntity.notFound().build();
        }

        String content = GoogleDriveUtil.downloadFileContent(target.getId());
        return ResponseEntity.ok(content);
    }

}

