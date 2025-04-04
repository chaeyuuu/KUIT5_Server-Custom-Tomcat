package controller;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpUrl;
import http.enums.Userquery;
import http.util.HttpRequestUtils;
import model.User;

import java.util.Map;

public class SignUpController implements Controller{
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, String> params = HttpRequestUtils.parseQueryParameter(request.getBody());

        User user = new User(params.get(Userquery.USER_ID.getKey()),
                params.get(Userquery.PASSWORD.getKey()),
                params.get(Userquery.NAME.getKey()),
                params.get(Userquery.EMAIL.getKey()));

        MemoryUserRepository.getInstance().addUser(user);
        response.redirect(HttpUrl.INDEX_URL.getPath());
    }
}
