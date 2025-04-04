package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpHeaders;
import http.enums.HttpUrl;
import http.enums.Userquery;
import http.util.HttpRequestUtils;
import model.User;

import java.io.IOException;
import java.util.Map;

public class LoginController implements Controller{
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, String> params = HttpRequestUtils.parseQueryParameter(request.getBody());

        String userId = params.get(Userquery.USER_ID.getKey());
        String password = params.get(Userquery.PASSWORD.getKey());

        User user = MemoryUserRepository.getInstance().findUserById(userId);
        checkIdAndPwd(user, password, response);
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

