package capstone.project.gaze_tracking_interest.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.StringReader;

public class GoogleSecretsFactory {
    private static final JacksonFactory JSON = JacksonFactory.getDefaultInstance();

    public static GoogleClientSecrets fromEnv() {
        String id  = System.getenv("CLIENT_ID");
        String sec = System.getenv("CLIENT_SECRET");
        if (id == null || sec == null) {
            throw new IllegalStateException("환경변수 CLIENT_ID / CLIENT_SECRET가 비어있습니다.");
        }
        String json = "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"" + id + "\",\n" +
                "    \"client_secret\": \"" + sec + "\",\n" +
                "    \"redirect_uris\": [\"http://localhost\"],\n" +
                "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "    \"token_uri\": \"https://oauth2.googleapis.com/token\"\n" +
                "  }\n" +
                "}";
        try {
            return GoogleClientSecrets.load(JSON, new StringReader(json));
        } catch (Exception e) {
            throw new RuntimeException("GoogleClientSecrets 생성 실패", e);
        }
    }
}
