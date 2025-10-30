package capstone.project.gaze_tracking_interest.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Drive 클라이언트 생성 유틸
 * - 서버(Render 등): 서비스 계정 JSON이 있으면 서비스 계정 사용
 * - 로컬: 환경변수 CLIENT_ID / CLIENT_SECRET 있으면 그걸로 Installed App OAuth
 * - 그 외: /etc/secrets/credentials.json 또는 classpath:/credentials.json
 * 공통: LocalServerReceiver 포트 0 사용(가용 포트 자동 할당) + Drive 싱글톤 재사용
 */
public class GoogleDriveUtil {

    private static final String APPLICATION_NAME = "MySpringDriveApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final com.google.api.client.http.HttpTransport HTTP_TRANSPORT = initHttpTransport();
    private static com.google.api.client.http.HttpTransport initHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init HTTP transport", e);
        }
    }

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    // 경로 정책
    private static final String USER_SECRET_PATH = "/etc/secrets/credentials.json";        // OAuth(Client ID)
    private static final String SERVICE_ACCOUNT_PATH = "/etc/secrets/service-account.json"; // Service Account

    // 싱글톤 캐시
    private static volatile Drive DRIVE;

    public static Drive getDriveService() throws IOException, GeneralSecurityException {
        if (DRIVE != null) return DRIVE;
        synchronized (GoogleDriveUtil.class) {
            if (DRIVE != null) return DRIVE;
            DRIVE = buildDriveOnce();
            return DRIVE;
        }
    }

    /** 실제 Drive 인스턴스는 여기서 한 번만 생성 */
    private static Drive buildDriveOnce() throws IOException, GeneralSecurityException {
        boolean hasServiceAccount = new java.io.File(SERVICE_ACCOUNT_PATH).exists();

        // 1) 서버(파일 존재)면 서비스 계정 우선
        if (hasServiceAccount) {
            System.out.println("🔐 Using Service Account");
            GoogleCredentials credentials;
            try (InputStream in = new FileInputStream(SERVICE_ACCOUNT_PATH)) {
                credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
            }
            HttpRequestInitializer rqInit = new HttpCredentialsAdapter(credentials);

            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, rqInit)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }

        // 2) 로컬/개발: 환경변수 CLIENT_ID / CLIENT_SECRET 우선
        GoogleClientSecrets clientSecrets = tryLoadClientSecretsFromEnv();
        if (clientSecrets == null) {
            // 3) 마지막으로 파일/클래스패스의 credentials.json 시도
            clientSecrets = tryLoadClientSecretsFromFileOrClasspath();
        }
        if (clientSecrets == null) {
            throw new IllegalStateException(
                    "No OAuth client secrets. Set ENV CLIENT_ID/CLIENT_SECRET or provide credentials.json."
            );
        }

        System.out.println("💻 Using Installed App OAuth (user consent)");

        java.io.File tokenDir = resolveTokenDir();
        if (!tokenDir.exists()) tokenDir.mkdirs();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(tokenDir))
                .setAccessType("offline")
                .build();

        // 포트 0: OS가 빈 포트 자동 할당 → 포트 충돌 방지
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(0)
                .build();

        Credential credential;
        try {
            credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");
        } finally {
            try { receiver.stop(); } catch (IOException ignore) {}
        }

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /** ENV에서 CLIENT_ID/CLIENT_SECRET 있으면 secrets 생성 */
    private static GoogleClientSecrets tryLoadClientSecretsFromEnv() {
        String id  = System.getenv("CLIENT_ID");
        String sec = System.getenv("CLIENT_SECRET");
        if (id == null || id.isBlank() || sec == null || sec.isBlank()) {
            return null;
        }
        try {
            return GoogleSecretsFactory.fromEnv(); // ✅ 당신이 만든 팩토리 사용
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GoogleClientSecrets from ENV", e);
        }
    }

    /** 파일(/etc/secrets 또는 classpath)에서 credentials.json 로드 */
    private static GoogleClientSecrets tryLoadClientSecretsFromFileOrClasspath() {
        try {
            InputStream in;
            java.io.File serverSecret = new java.io.File(USER_SECRET_PATH);
            if (serverSecret.exists()) {
                in = new FileInputStream(serverSecret);
                System.out.println("🔐 credentials.json from /etc/secrets");
            } else {
                in = GoogleDriveUtil.class.getResourceAsStream("/credentials.json");
                if (in != null) {
                    System.out.println("📦 credentials.json from classpath");
                }
            }
            if (in == null) return null;

            try (Reader reader = new InputStreamReader(in)) {
                return GoogleClientSecrets.load(JSON_FACTORY, reader);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials.json", e);
        }
    }

    private static java.io.File resolveTokenDir() {
        // 서버에서도 토큰을 저장하고 싶으면 보안 경로 사용
        if (new java.io.File("/etc/secrets").exists()) {
            return new java.io.File("/etc/secrets/tokens");
        }
        String userHome = System.getProperty("user.home");
        return new java.io.File(userHome, ".gdrive_tokens");
    }

    // ===================== Public APIs =====================

    public static List<File> listFilesInFolder(String folderId, String mimeTypeFilter)
            throws IOException, GeneralSecurityException {
        Drive service = getDriveService();

        String query = String.format("'%s' in parents and mimeType contains '%s' and trashed = false",
                folderId, mimeTypeFilter);

        FileList result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, webViewLink, webContentLink)")
                .execute();

        return result.getFiles();
    }

    public static String downloadFileContent(String fileId)
            throws IOException, GeneralSecurityException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Drive driveService = getDriveService();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
