package http;

import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;


    public HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        String requestLine = br.readLine();
        String[] tokens = requestLine.split(" ");
        String method = tokens[0];
        String path = tokens[1];
        String version = tokens[2];

        Map<String, String> headers = new HashMap<>();
        int contentLength = getContentLength(br, headers);

        String body = IOUtils.readData(br, contentLength);

        return new HttpRequest(method, path, version, headers, body);
    }

    private static int getContentLength(BufferedReader br, Map<String, String> headers) throws IOException {
        int contentLength = 0;
        String line;
        while (!(line = br.readLine()).equals("")) { // 요청 헤더 읽기
            String[] parts = line.split(": ", 2); // : 기준으로 나눠서 저장
//            System.out.println(line);
            if (parts.length == 2) {
                headers.put(parts[0], parts[1]);
                if (parts[0].equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(parts[1]);
                }
            }
        }
        return contentLength;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}

