package capstone.project.gaze_tracking_interest.datasource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** sample-data/ 폴더의 파일을 읽는 구현. 하위 폴더는 보지 않는다. */
@Service
@ConditionalOnProperty(name = "app.data-source", havingValue = "local", matchIfMissing = true)
public class LocalDataSourceService implements DataSourceService {

    private static final String VIDEO_URL = "/api/drive/video?name=";

    private final Path dataDir;

    public LocalDataSourceService(@Value("${app.local.data-dir:sample-data}") String dataDir) {
        this.dataDir = Path.of(dataDir).toAbsolutePath().normalize();
    }

    @Override
    public List<DataFile> listFiles() throws IOException {
        if (!Files.isDirectory(dataDir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dataDir)) {
            return stream.filter(Files::isRegularFile)
                    .map(this::toDataFile)
                    .toList();
        }
    }

    @Override
    public List<DataFile> listFiles(String folderId, String mimeType) throws IOException {
        return listFiles().stream()
                .filter(f -> mimeType == null || mimeType.isBlank() || mimeType.equals(f.mimeType()))
                .toList();
    }

    @Override
    public String readContent(DataFile file) throws IOException {
        return Files.readString(resolve(file.name()), StandardCharsets.UTF_8);
    }

    @Override
    public Optional<Resource> openVideo(String name) throws IOException {
        // 목록에 있는 영상 이름만 허용 (../ 같은 경로 조작 차단)
        boolean listed = listVideos().stream().anyMatch(f -> f.name().equals(name));
        return listed ? Optional.of(new FileSystemResource(resolve(name))) : Optional.empty();
    }

    private Path resolve(String name) throws IOException {
        Path p = dataDir.resolve(name).normalize();
        if (!p.startsWith(dataDir) || !Files.isRegularFile(p)) {
            throw new IOException("파일을 찾을 수 없습니다: " + name);
        }
        return p;
    }

    private DataFile toDataFile(Path p) {
        String name = p.getFileName().toString();
        String mime = URLConnection.guessContentTypeFromName(name);
        String link = VIDEO_URL + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8);
        long modified;
        try {
            modified = Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            modified = 0L;
        }
        return new DataFile(name, name, mime != null ? mime : "application/octet-stream", link, link, modified);
    }
}
