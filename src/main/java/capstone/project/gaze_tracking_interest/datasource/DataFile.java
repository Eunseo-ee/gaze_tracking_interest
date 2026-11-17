package capstone.project.gaze_tracking_interest.datasource;

/** 데이터 소스(Drive/로컬)에 상관없이 쓰는 파일 메타데이터. */
public record DataFile(
        String id,
        String name,
        String mimeType,
        String webViewLink,
        String webContentLink,
        long modifiedTime
) {}
