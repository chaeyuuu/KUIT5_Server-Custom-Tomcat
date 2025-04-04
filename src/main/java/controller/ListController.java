package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpHeaders;
import http.enums.HttpUrl;
import http.enums.Userquery;
import http.util.HttpRequestUtils;
import model.User;

import java.util.Map;

public class ListController implements Controller {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        String cookie = request.getHeader(HttpHeaders.COOKIE.getHttpHeaders());
        boolean isCookie = cookie != null && cookie.contains("logined=true");

        if (isCookie) {
            response.forward("./webapp" + HttpUrl.LIST.getPath());
        } else {
            response.redirect(HttpUrl.LOGIN.getPath() + ".html");
        }
    }
}
