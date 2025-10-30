package capstone.project.gaze_tracking_interest.controller;

import capstone.project.gaze_tracking_interest.service.GazeAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analyze")
@CrossOrigin(origins = "*")  // 🔹 CORS 허용 (로컬/Render 연결용)
public class GazeController {

    private final GazeAnalysisService gazeService;

    public GazeController(GazeAnalysisService gazeService) {
        this.gazeService = gazeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> analyze(@RequestParam String driveLink) {
        Map<String, Object> result = gazeService.analyze(driveLink);
        return ResponseEntity.ok(result);
    }
}
