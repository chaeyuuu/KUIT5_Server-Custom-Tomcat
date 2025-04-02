package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
            String method = tokens[0];
            String url = tokens[1];

            String path = url;
            String queryString = "";

            // 요구사항 1
            if (url.equals("/")) {
                url = "/index.html";
            }

            // 요구사항 2
            if (url.contains("?")) {
                String[] parts = url.split("\\?");
                path = parts[0]; // "/user/signup"
                queryString = parts[1]; // "userId=chaeyu&password=1234&..."
            }

            // 요구사항 3
            if (path.equals("/user/signup")) {
                Map<String, String> params = null;

                if (method.equals("POST")) {
                    // post 방식에는 queryString이 request body 안에 들어있음 -> Header의 Content-Length 값 필요
                    String body = getContentLength(br);
                    params = HttpRequestUtils.parseQueryParameter(body);
                }

                User user = new User(params.get("userId"),
                        params.get("password"),
                        params.get("name"),
                        params.get("email"));

                MemoryUserRepository.getInstance().addUser(user);
                response302Header(dos, "/index.html");
                return;
            }

            // 요구사항 5 - 로그인
            if (path.equals("/user/login") && method.equals("POST")) {
                String body = getContentLength(br);
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                String userId = params.get("userId");
                String password = params.get("password");

                User user = MemoryUserRepository.getInstance().findUserById(userId);
                if (user != null && user.getPassword().equals(password)) {
                    response302HeaderWithCookie(dos, "/index.html", "logined=true");
                } else {
                    response302Header(dos, "/user/login_failed.html");
                }
                return;
            }

            if (path.equals("/user/userList")){
                boolean isCookie = false;

                while (true) {
                    final String line = br.readLine();
                    if (line.equals("")) {
                        break;
                    }

                    if (line.startsWith("Cookie:")) {
                        String cookies = line.split(": ")[1];
                        String[] pairs = cookies.split(";");

                        for(String pair: pairs){
                            String[] keyValue = pair.trim().split("=");
                            if (keyValue.length == 2 && keyValue[0].equals("logined") && keyValue[1].equals("true")) {
                                isCookie = true;
                                break;
                            }
                        }

                    }
                }

                if (isCookie) {
                    byte[] body = Files.readAllBytes(Paths.get("./webapp/user/list.html"));
                    response200Header(dos, url, body.length);
                    responseBody(dos, body);
                } else {
                    response302Header(dos, "/user/login.html");
                }

                return;
            }

            // 파일 내용 반환
            byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));

            // byte[] body = "Hello World".getBytes();
            response200Header(dos, url, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private static String getContentLength(BufferedReader br) throws IOException {
        int requestContentLength = 0;
        while (true) {
            final String line = br.readLine();
            if (line.equals("")) {
                break;
            }
            // header info
            if (line.startsWith("Content-Length")) {
                requestContentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }
        String body = IOUtils.readData(br, requestContentLength);
        return body;
    }

    // 헤더에 Cookie: logined=true를 추가하고, index.html 화면으로 redirect
    private void response302HeaderWithCookie(DataOutputStream dos, String path, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
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

    private void response200Header(DataOutputStream dos, String url, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if (url.endsWith(".css")) {
                dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            } else {
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            }            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
