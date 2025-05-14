package capstone.project.gaze_tracking_interest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileListController {
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        Path savePath = Paths.get("src/main/resources/static/uploads/" + fileName);
        Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("업로드 완료: " + fileName);
    }

    @GetMapping("/csv-list")
    public ResponseEntity<List<String>> getCSVList() throws IOException {
        Path uploadPath = Paths.get("src/main/resources/static/uploads/");
        try (Stream<Path> paths = Files.list(uploadPath)) {
            List<String> csvFiles = paths
                    .filter(p -> p.toString().endsWith(".csv"))
                    .map(p -> "/uploads/" + p.getFileName().toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(csvFiles);
        }
    }

}
