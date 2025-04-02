package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // 요청 메시지 읽어들이기
            String requestLine = br.readLine();
            if (requestLine == null) return;

            String[] tokens = requestLine.split(" ");
            String url = tokens[1];

            String path = url;
            String queryString = "";

            // 요구사항 2
            if (url.contains("?")) {
                String[] parts = url.split("\\?");
                path = parts[0]; // "/user/signup"
                queryString = parts[1]; // "userId=chaeyu&password=1234&..."
            }

            if (path.equals("/user/signup")) {
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(queryString);

                User user = new User(params.get("userId"),
                        params.get("password"),
                        params.get("name"),
                        params.get("email"));

                MemoryUserRepository.getInstance().addUser(user);
                response302Header(dos, "/index.html");
               return;
            }

            // 요구사항 1
            if (url.equals("/")) {
                url = "/index.html";
            }

            // 파일 내용 반환
            byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));

            // byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}
