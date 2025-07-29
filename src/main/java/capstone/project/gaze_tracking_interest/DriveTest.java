package capstone.project.gaze_tracking_interest;

import capstone.project.gaze_tracking_interest.config.GoogleDriveUtil;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveTest {
    public static void main(String[] args) {
        try {
            System.out.println("✅ DriveTest 실행 시작");

            Drive service = GoogleDriveUtil.getDriveService();

            // capstone-design 폴더 ID
            String folderId = "1ZRAfqwSe7vnxMqN6rlu9KcJxTmMvxMBz";

            FileList result = service.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("files(id, name, mimeType)")
                    .execute();

            System.out.println("📁 capstone-design 폴더 내 파일 목록:");
            for (File file : result.getFiles()) {
                System.out.printf("🗂 %s (%s, %s)\n", file.getName(), file.getId(), file.getMimeType());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
