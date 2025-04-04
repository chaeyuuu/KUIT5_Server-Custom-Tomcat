package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpUrl;

public class HomeController implements Controller{
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        response.forward("./webapp" + HttpUrl.INDEX_URL.getPath());
    }
}
