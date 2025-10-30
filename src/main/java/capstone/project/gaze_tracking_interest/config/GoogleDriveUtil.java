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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GoogleDriveUtil {

    private static final String APPLICATION_NAME = "GazeTrackingInterest";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private static final com.google.api.client.http.HttpTransport HTTP_TRANSPORT = initHttpTransport();

    private static com.google.api.client.http.HttpTransport initHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init HTTP transport", e);
        }
    }

    private static final String CREDENTIALS_PATH = "/etc/secrets/credentials.json";
    private static final String TOKEN_PATH = "/etc/secrets/drive_token.json";
    private static final String LOCAL_CREDENTIALS_PATH = "src/main/resources/credentials.json";
    private static final String LOCAL_TOKEN_DIR = "src/main/resources/.gdrive_tokens";

    private static volatile Drive DRIVE;

    /** ✅ Drive 싱글톤 객체 반환 */
    public static Drive getDriveService() throws IOException, GeneralSecurityException {
        if (DRIVE != null) return DRIVE;
        synchronized (GoogleDriveUtil.class) {
            if (DRIVE != null) return DRIVE;
            DRIVE = buildDriveOnce();
            return DRIVE;
        }
    }

    /** ✅ Render + 로컬 통합 인증 */
    private static Drive buildDriveOnce() throws IOException, GeneralSecurityException {

        // ✅ Render secrets 경로
        Path tokenPath = Paths.get("/etc/secrets/drive_token.json");
        Path serviceAccountPath = Paths.get("/etc/secrets/service-account.json");

        // ✅ Render 배포 환경이면 (drive_token.json 우선)
        if (Files.exists(tokenPath)) {
            System.out.println("🔐 Using pre-issued drive_token.json from Render");
            try (InputStream in = Files.newInputStream(tokenPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                        .createScoped(SCOPES);
                HttpRequestInitializer rqInit = new HttpCredentialsAdapter(credentials);
                return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, rqInit)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            }
        }

        // ✅ 서비스 계정이 있으면 그걸로 인증
        if (Files.exists(serviceAccountPath)) {
            System.out.println("🧾 Using Service Account JSON");
            try (InputStream in = Files.newInputStream(serviceAccountPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                        .createScoped(SCOPES);
                HttpRequestInitializer rqInit = new HttpCredentialsAdapter(credentials);
                return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, rqInit)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            }
        }

        // ✅ 로컬 OAuth (credentials.json + .gdrive_tokens)
        System.out.println("💻 Using local OAuth flow");
        InputStream in = new FileInputStream(LOCAL_CREDENTIALS_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        java.io.File tokenDir = new java.io.File(LOCAL_TOKEN_DIR);
        if (!tokenDir.exists()) tokenDir.mkdirs();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenDir))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(0)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // ✅ 여기부터 아래 두 메서드 추가 (Controller에서 호출되는 부분)

    /**
     * 📂 특정 폴더의 파일 목록 조회
     */
    public static List<File> listFilesInFolder(String folderId, String mimeTypeFilter)
            throws IOException, GeneralSecurityException {

        Drive service = getDriveService();

        // MIME 타입 필터링이 있으면 추가
        String query = String.format("'%s' in parents and trashed = false", folderId);
        if (mimeTypeFilter != null && !mimeTypeFilter.isBlank()) {
            query += String.format(" and mimeType contains '%s'", mimeTypeFilter);
        }

        FileList result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, webViewLink, webContentLink)")
                .execute();

        return result.getFiles();
    }

    /**
     * 📥 파일 ID로 Drive 파일 내용 다운로드
     */
    public static String downloadFileContent(String fileId)
            throws IOException, GeneralSecurityException {
        Drive service = getDriveService();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
