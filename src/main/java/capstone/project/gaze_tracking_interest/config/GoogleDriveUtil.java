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

public class GoogleDriveUtil {

    private static final String APPLICATION_NAME = "MySpringDriveApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // 공통 HTTP_TRANSPORT는 1회 생성
    private static final com.google.api.client.http.HttpTransport HTTP_TRANSPORT = initHttpTransport();

    private static com.google.api.client.http.HttpTransport initHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init HTTP transport", e);
        }
    }

    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE);

    private static final String USER_SECRET_PATH = "/etc/secrets/credentials.json";       // OAuth(Client ID)
    private static final String SERVICE_ACCOUNT_PATH = "/etc/secrets/service-account.json"; // Service Account

    // ✅ 싱글톤 캐시 (동시성 안전)
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
        boolean hasUserSecret = new java.io.File(USER_SECRET_PATH).exists() ||
                GoogleDriveUtil.class.getResourceAsStream("/credentials.json") != null;

        if (isRenderEnv() && hasServiceAccount) {
            // ✅ Render(서버) 환경: 서비스 계정 사용 (브라우저 불필요)
            System.out.println("🔐 Using Service Account on server");
            GoogleCredentials credentials;
            try (InputStream in = new FileInputStream(SERVICE_ACCOUNT_PATH)) {
                credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
            }
            HttpRequestInitializer rqInit = new HttpCredentialsAdapter(credentials);

            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, rqInit)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } else if (hasUserSecret) {
            // ✅ 로컬: 사용자 OAuth (최초 1회 브라우저 인증)
            System.out.println("💻 Using Installed App OAuth locally");
            GoogleClientSecrets clientSecrets = loadClientSecrets();

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

            // ★ 포트 0으로 설정 → OS가 가용 포트 임의 할당
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

        throw new IllegalStateException("No valid credentials found. "
                + "Provide Service Account at " + SERVICE_ACCOUNT_PATH
                + " for server, or user OAuth credentials.json in /etc/secrets or classpath for local.");
    }

    private static GoogleClientSecrets loadClientSecrets() throws IOException {
        InputStream in;
        java.io.File serverSecret = new java.io.File(USER_SECRET_PATH);
        if (serverSecret.exists()) {
            in = new FileInputStream(serverSecret);
            System.out.println("🔐 credentials.json from /etc/secrets");
        } else {
            in = GoogleDriveUtil.class.getResourceAsStream("/credentials.json");
            System.out.println("📦 credentials.json from classpath");
        }
        if (in == null) throw new FileNotFoundException("credentials.json not found");
        try (Reader reader = new InputStreamReader(in)) {
            return GoogleClientSecrets.load(JSON_FACTORY, reader);
        }
    }

    private static java.io.File resolveTokenDir() {
        if (isRenderEnv()) {
            // Render에 토큰 디렉토리를 비밀 마운트로 잡고 싶다면 이 경로 사용
            return new java.io.File("/etc/secrets/tokens");
        }
        String userHome = System.getProperty("user.home");
        return new java.io.File(userHome, ".gdrive_tokens");
    }

    private static boolean isRenderEnv() {
        // 환경 변수 등으로 구분(원하는 방식으로 바꿔도 됨)
        // Render에서는 흔히 RENDER=true 같은 변수를 세팅하거나,
        // 단순히 서비스 계정 파일/시크릿 존재 여부로 판단할 수도 있음.
        return new java.io.File("/etc/secrets").exists();
    }

    // ===================== Public APIs =====================

    public static List<File> listFilesInFolder(String folderId, String mimeTypeFilter)
            throws IOException, GeneralSecurityException {
        Drive service = getDriveService();

        // mimeType이 정확히 정해져 있으면 equals로 쓰는 것도 고려 (예: 'text/csv')
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
