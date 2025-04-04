package webserver;

import controller.*;
import http.HttpRequest;
import http.HttpResponse;
import http.enums.HttpUrl;

public class RequestMapper {
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private Controller controller = new ForwardController();


    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public void proceed() throws Exception {
        String method = httpRequest.getMethod();
        String url = httpRequest.getPath();

        if (method.equals("GET") && url.endsWith(".html")) {
            controller = new ForwardController();
        }

        if (url.equals(HttpUrl.ROOT.getPath())) {
            controller = new HomeController();
        }

        if (url.equals(HttpUrl.SIGN_UP.getPath())) {
            controller = new SignUpController();
        }

        if (url.equals(HttpUrl.LOGIN.getPath())) {
            controller = new LoginController();
        }

        if (url.equals(HttpUrl.USER_LIST.getPath())) {
            controller = new ListController();
        }
        controller.execute(httpRequest, httpResponse);
    }
}
