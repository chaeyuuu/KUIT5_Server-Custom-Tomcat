package http;

import http.enums.HttpHeaders;
import http.enums.HttpStatus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void forward(String path) throws IOException {
        byte[] body = Files.readAllBytes(Paths.get("./webapp" + path));
        addHeader(HttpHeaders.CONTENT_TYPE.getHttpHeaders(), contentType(path));
        addHeader(HttpHeaders.CONTENT_LENGTH.getHttpHeaders(), String.valueOf(body.length));
        writeResponse(HttpStatus.OK.getStatus(), body);
    }

    public void redirect(String path) throws IOException {
        addHeader(HttpHeaders.LOCATION.getHttpHeaders(), path);
        writeResponse(HttpStatus.FOUND.getStatus(), new byte[0]);
    }

    public void forwardWithCookie(String path, String cookie) throws IOException {
        addHeader(HttpHeaders.SET_COOKIE.getHttpHeaders(), cookie);
        forward(path);
    }

    private void writeResponse(String statusLine, byte[] body) throws IOException {
        dos.writeBytes(statusLine);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private String contentType(String path) {
        if (path.endsWith(".css")) return "text/css;charset=utf-8";
        return "text/html;charset=utf-8";
    }



}
