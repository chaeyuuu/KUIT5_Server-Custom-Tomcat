package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {
    

    @Test
    @DisplayName("HttpRequest 메시지를 파일로부터 읽어 정확히 파싱하는지 테스트")
    void parseHttpRequestFromFile() throws Exception {
        // given
        String filePath = "src/test/resources/request.txt";
        BufferedReader br = bufferedReaderFromFile(filePath);

        // when
        HttpRequest request = HttpRequest.from(br);

        // then
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/user/create");
        assertThat(request.getHeaders().get("Host")).isEqualTo("localhost:8080");
        assertThat(request.getHeaders().get("Content-Length")).isEqualTo("40");
        assertThat(request.getBody()).isEqualTo("userId=jw&password=password&name=jungwoo");
    }

    private BufferedReader bufferedReaderFromFile(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(
                Files.newInputStream(Paths.get(path))));
    }
}