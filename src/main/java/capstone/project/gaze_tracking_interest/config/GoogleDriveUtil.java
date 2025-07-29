package capstone.project.gaze_tracking_interest.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveUtil {

    private static final String APPLICATION_NAME = "MySpringDriveApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/drive"); // 직접 입력
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static com.google.api.services.drive.Drive getDriveService() throws IOException, GeneralSecurityException {
        InputStream in = GoogleDriveUtil.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: credentials.json");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new com.google.api.services.drive.Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }

    public static List<File> listFilesInFolder(String folderId, String mimeTypeFilter) throws IOException, GeneralSecurityException {
        Drive service = getDriveService();

        // 폴더 내 특정 mimeType을 가진 파일만 조회
        String query = String.format("'%s' in parents and mimeType contains '%s' and trashed = false", folderId, mimeTypeFilter);

        FileList result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, webViewLink, webContentLink)")
                .execute();

        return result.getFiles();
    }

    public static String downloadFileContent(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            com.google.api.services.drive.Drive driveService = getDriveService(); // ✅ 여기!
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        } catch (Exception e) {
            throw new IOException("Failed to download file from Google Drive", e);
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
