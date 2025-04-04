package http.enums;

public enum HttpHeaders {
    CONTENT_LENGTH("Content-Length"),
    SET_COOKIE("Set-Cookie"),
    COOKIE("Cookie"),
    LOCATION("Location"),
    CONTENT_TYPE("Content-Type");

    private final String httpHeaders;

    HttpHeaders(String httpheaders) {
        this.httpHeaders = httpheaders;
    }

    public String getHttpHeaders(){
        return httpHeaders;
    }
}
