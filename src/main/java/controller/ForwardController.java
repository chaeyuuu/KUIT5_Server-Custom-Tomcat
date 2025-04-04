package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpUrl;

public class ForwardController implements Controller{

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws Exception {
        String url = request.getPath();
        if (url.equals(HttpUrl.ROOT.getPath())) {
            url = HttpUrl.INDEX_URL.getPath();
        }
        response.forward("./webapp" + url);
    }
}
