package capstone.project.gaze_tracking_interest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;   // @RestController, @RequestMapping, @PostMapping 등
import org.springframework.web.client.RestTemplate;  // FastAPI 호출용
import java.util.Map;  // Map<String, String> 사용
import org.springframework.ui.Model;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/analyze")
    public String analyze(@RequestParam String driveLink, Model model) {
        String fastApiUrl = "https://gaze-fastapi.onrender.com/process";
        Map<String, String> body = Map.of("drive_link", driveLink);

        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, body, Map.class);
        model.addAttribute("analyze", response.getBody());
        return "analyze"; // templates/analyze.html
    }

}
