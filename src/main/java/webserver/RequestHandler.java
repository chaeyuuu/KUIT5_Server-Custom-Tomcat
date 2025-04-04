package webserver;

import db.MemoryUserRepository;
import http.enums.*;
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

//             BufferedReader.readLine()을 한 번만 호출해서 모든 헤더를 읽고 재사용
//            List<String> headers = new ArrayList<>();
//            String line;
//            while (!(line = br.readLine()).equals("")) {
//                headers.add(line);
//            }

            // 요구사항 1
//            if (url.equals("/")) {
            if (url.equals(HttpUrl.ROOT.getPath())) {
                url = HttpUrl.INDEX_URL.getPath();
            }

            // 요구사항 2
            if (url.contains("?")) {
                String[] parts = url.split("\\?");
                path = parts[0]; // "/user/signup"
                queryString = parts[1]; // "userId=chaeyu&password=1234&..."
            }

            // 요구사항 3
            if (path.equals(HttpUrl.SIGN_UP.getPath())) {
                Map<String, String> params = null;

                if (method.equals("POST")) {
                    // post 방식에는 queryString이 request body 안에 들어있음 -> Header의 Content-Length 값 필요
                    String body = getContentLength(br);
                    params = HttpRequestUtils.parseQueryParameter(body);
                }

                User user = new User(params.get(Userquery.USER_ID.getKey()),
                        params.get(Userquery.PASSWORD.getKey()),
                        params.get(Userquery.NAME.getKey()),
                        params.get(Userquery.EMAIL.getKey()));

                MemoryUserRepository.getInstance().addUser(user);
                response302Header(dos, HttpUrl.INDEX_URL.getPath());
                return;
            }

            // 요구사항 5 - 로그인
            if (path.equals(HttpUrl.LOGIN.getPath()) && method.equals("POST")) {
                String body = getContentLength(br);
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                String userId = params.get(Userquery.USER_ID.getKey());
                String password = params.get(Userquery.PASSWORD.getKey());

                User user = MemoryUserRepository.getInstance().findUserById(userId);
                if (user != null && user.getPassword().equals(password)) {
                    response302HeaderWithCookie(dos, HttpUrl.INDEX_URL.getPath(), "logined=true");
                } else {
                    response302Header(dos, HttpUrl.LOGIN_FAILED.getPath());
                }
                return;
            }

            // user list 반환
            if (path.equals(HttpUrl.USER_LIST.getPath())){
                boolean isCookie = false;

                while (true) {
                    final String line = br.readLine();
                    if (line.equals("")) {
                        break;
                    }

                    if (line.startsWith(HttpHeaders.COOKIE.getHttpHeaders()+":")) {
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
                    byte[] body = Files.readAllBytes(Paths.get("./webapp"+HttpUrl.LIST.getPath()));
                    response200Header(dos, url, body.length);
                    responseBody(dos, body);
                    // System.out.println("Serving file: " + filepath);
                } else {
                    response302Header(dos, HttpUrl.LOGIN.getPath()+".html");
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
            if (line.startsWith(HttpHeaders.CONTENT_LENGTH.getHttpHeaders())) {
                requestContentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }
        String body = IOUtils.readData(br, requestContentLength);
        return body;
    }

    // 헤더에 Cookie: logined=true를 추가하고, index.html 화면으로 redirect
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
