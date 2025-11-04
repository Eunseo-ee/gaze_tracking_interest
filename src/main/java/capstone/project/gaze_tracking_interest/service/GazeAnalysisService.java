package capstone.project.gaze_tracking_interest.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GazeAnalysisService {

    // 🔹 로컬 FastAPI 주소 (Render에 올리면 아래 url 수정)
    private final WebClient webClient = WebClient.create("https://unsiccative-jada-unsoundable.ngrok-free.dev");

    public Map<String, Object> analyze(String driveLink) {
        return webClient.post()
                .uri("/process")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("driveLink", driveLink))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
