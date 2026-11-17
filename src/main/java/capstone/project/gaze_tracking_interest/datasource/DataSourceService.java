package capstone.project.gaze_tracking_interest.datasource;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** 시선/판매 CSV와 영상 파일을 읽어오는 데이터 소스. app.data-source=local|drive 로 구현체가 선택된다. */
public interface DataSourceService {

    /** 기본 폴더의 전체 파일 목록. */
    List<DataFile> listFiles() throws IOException;

    /** 지정한 폴더/MIME 타입의 파일 목록. 로컬 모드는 folderId를 무시한다. */
    List<DataFile> listFiles(String folderId, String mimeType) throws IOException;

    /** 파일 내용을 UTF-8 문자열로 읽는다. */
    String readContent(DataFile file) throws IOException;

    /** 로컬 영상 스트리밍용. Drive 모드는 Drive 링크를 쓰므로 비어 있다. */
    default Optional<Resource> openVideo(String name) throws IOException {
        return Optional.empty();
    }

    default List<DataFile> listVideos() throws IOException {
        return listFiles().stream()
                .filter(f -> f.name().toLowerCase().replaceAll("\\s", "").matches(".*\\.(mp4|avi|mov|mkv)$"))
                .toList();
    }

    default List<String> listCsvNames() throws IOException {
        return listFiles().stream()
                .map(DataFile::name)
                .filter(n -> n.toLowerCase().endsWith(".csv"))
                .toList();
    }

    default Optional<DataFile> findByName(String name) throws IOException {
        return listFiles().stream().filter(f -> f.name().equals(name)).findFirst();
    }

    /** gaze-tracking*.csv 중 가장 최근에 수정된 파일. */
    default Optional<DataFile> findLatestGazeCsv() throws IOException {
        return listFiles().stream()
                .filter(f -> {
                    String n = f.name().toLowerCase();
                    return n.startsWith("gaze-tracking") && n.endsWith(".csv");
                })
                .max(Comparator.comparingLong(DataFile::modifiedTime));
    }
}
