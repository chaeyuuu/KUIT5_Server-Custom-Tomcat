package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
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

            HttpResponse httpResponse = new HttpResponse(out);
            HttpRequest httpRequest = HttpRequest.from(br);
            String method = httpRequest.getMethod();
            String url = httpRequest.getPath();
            String body = httpRequest.getBody();

            // 요구사항 1
            if (url.equals(HttpUrl.ROOT.getPath())) {
                url = HttpUrl.INDEX_URL.getPath();
            }


            // 회원가입
            if (url.equals(HttpUrl.SIGN_UP.getPath()) && method.equals("POST")) {
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                User user = new User(params.get(Userquery.USER_ID.getKey()),
                        params.get(Userquery.PASSWORD.getKey()),
                        params.get(Userquery.NAME.getKey()),
                        params.get(Userquery.EMAIL.getKey()));

                MemoryUserRepository.getInstance().addUser(user);
                httpResponse.redirect(HttpUrl.INDEX_URL.getPath());
                return;
            }

            // 요구사항 5 - 로그인
            if (url.equals(HttpUrl.LOGIN.getPath()) && method.equals("POST")) {
                Map<String, String> params = HttpRequestUtils.parseQueryParameter(body);

                String userId = params.get(Userquery.USER_ID.getKey());
                String password = params.get(Userquery.PASSWORD.getKey());

                User user = MemoryUserRepository.getInstance().findUserById(userId);
                checkIdAndPwd(user, password, httpResponse);
                return;
            }

            // user list 반환
            if (url.equals(HttpUrl.USER_LIST.getPath())) {
                String cookie = httpRequest.getHeader(HttpHeaders.COOKIE.getHttpHeaders());
                boolean isCookie = cookie != null && cookie.contains("logined=true");

                if (isCookie) {
                    httpResponse.forward("./webapp" + HttpUrl.LIST.getPath());
                } else {
                    httpResponse.redirect(HttpUrl.LOGIN.getPath() + ".html");
                }
                return;
            }

            httpResponse.forward("./webapp" + url);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void checkIdAndPwd(User user, String password, HttpResponse httpResponse) throws IOException {
        if (user != null && user.getPassword().equals(password)) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE.getHttpHeaders(), "logined=true");
            httpResponse.redirect(HttpUrl.INDEX_URL.getPath());
        } else {
            httpResponse.redirect(HttpUrl.LOGIN_FAILED.getPath());
        }
    }
}