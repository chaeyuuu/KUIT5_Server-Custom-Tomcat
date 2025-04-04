package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.enums.*;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
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

            HttpRequest httpRequest = HttpRequest.from(br);
            String method = httpRequest.getMethod();
            String url = httpRequest.getPath();
            String body = httpRequest.getBody();

            // 요구사항 1
//            if (url.equals("/")) {
            if (url.equals(HttpUrl.ROOT.getPath())) {
                url = HttpUrl.INDEX_URL.getPath();
            }

//            // 요구사항 2
//            if (url.contains("?")) {
//                String[] parts = url.split("\\?");
//                path = parts[0]; // "/user/signup"
//                queryString = parts[1]; // "userId=chaeyu&password=1234&..."
//            }

            // 회원가입
            if (url.equals(HttpUrl.SIGN_UP.getPath()) && method.equals("POST")) {
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                User user = new User(params.get(Userquery.USER_ID.getKey()),
                        params.get(Userquery.PASSWORD.getKey()),
                        params.get(Userquery.NAME.getKey()),
                        params.get(Userquery.EMAIL.getKey()));

                MemoryUserRepository.getInstance().addUser(user);
                response302Header(dos, HttpUrl.INDEX_URL.getPath());
                return;
            }

            // 요구사항 5 - 로그인
            if (url.equals(HttpUrl.LOGIN.getPath()) && method.equals("POST")) {
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                String userId = params.get(Userquery.USER_ID.getKey());
                String password = params.get(Userquery.PASSWORD.getKey());

                User user = MemoryUserRepository.getInstance().findUserById(userId);
                checkIdAndPwd(user, password, dos);
                return;
            }

            // user list 반환
            if (url.equals(HttpUrl.USER_LIST.getPath())){
                String cookie = httpRequest.getHeader(HttpHeaders.COOKIE.getHttpHeaders());
                boolean isCookie = cookie != null && cookie.contains("logined=true");

                if (isCookie) {
                    byte[] fileBody = Files.readAllBytes(Paths.get("./webapp"+HttpUrl.LIST.getPath()));
                    response200Header(dos, url, fileBody.length);
                    responseBody(dos, fileBody);
                    // System.out.println("Serving file: " + filepath);
                } else {
                    response302Header(dos, HttpUrl.LOGIN.getPath()+".html");
                }
                return;
            }

            // 파일 내용 반환
            byte[] fileBody = Files.readAllBytes(Paths.get("./webapp" + url));

            // byte[] body = "Hello World".getBytes();
            response200Header(dos, url, fileBody.length);
            responseBody(dos, fileBody);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void checkIdAndPwd(User user, String password, DataOutputStream dos) {
        if (user != null && user.getPassword().equals(password)) {
            response302HeaderWithCookie(dos, HttpUrl.INDEX_URL.getPath(), "logined=true");
        } else {
            response302Header(dos, HttpUrl.LOGIN_FAILED.getPath());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String path, String cookie) {
        try {
            dos.writeBytes(HttpStatus.FOUND.getStatus());
            dos.writeBytes(HttpHeaders.LOCATION.getHttpHeaders()+": "+ path + "\r\n");
            dos.writeBytes(HttpHeaders.SET_COOKIE.getHttpHeaders()+": "+ cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes(HttpStatus.FOUND.getStatus());
            dos.writeBytes(HttpHeaders.LOCATION.getHttpHeaders()+": "+ path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, String url, int lengthOfBodyContent) {
        try {
            dos.writeBytes(HttpStatus.OK.getStatus());
            if (url.endsWith(".css")) {
                dos.writeBytes(HttpHeaders.CONTENT_TYPE.getHttpHeaders()+": text/css;charset=utf-8\r\n");
            } else {
                dos.writeBytes(HttpHeaders.CONTENT_TYPE.getHttpHeaders()+": text/html;charset=utf-8\r\n");
            }
            dos.writeBytes(HttpHeaders.CONTENT_LENGTH.getHttpHeaders() + ": " + lengthOfBodyContent + "\r\n");
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
