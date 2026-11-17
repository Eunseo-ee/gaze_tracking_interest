package capstone.project.gaze_tracking_interest.datasource;

import capstone.project.gaze_tracking_interest.config.GoogleDriveUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.data-source", havingValue = "drive")
public class DriveDataSourceService implements DataSourceService {

    private final String folderId;

    public DriveDataSourceService(@Value("${app.drive.folder-id:}") String folderId) {
        this.folderId = folderId;
    }

    @Override
    public List<DataFile> listFiles() throws IOException {
        return listFiles(folderId, null);
    }

    @Override
    public List<DataFile> listFiles(String folderId, String mimeType) throws IOException {
        if (folderId == null || folderId.isBlank()) {
            throw new IOException("Drive 폴더 ID가 설정되지 않았습니다. 환경변수 DRIVE_FOLDER_ID를 지정하세요.");
        }
        try {
            return GoogleDriveUtil.listFilesInFolder(folderId, mimeType).stream()
                    .map(f -> new DataFile(
                            f.getId(),
                            f.getName(),
                            f.getMimeType(),
                            f.getWebViewLink(),
                            f.getWebContentLink(),
                            f.getModifiedTime() != null ? f.getModifiedTime().getValue() : 0L))
                    .toList();
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String readContent(DataFile file) throws IOException {
        try {
            return GoogleDriveUtil.downloadFileContent(file.id());
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
