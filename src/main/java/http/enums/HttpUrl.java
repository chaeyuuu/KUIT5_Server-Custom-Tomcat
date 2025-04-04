package http.enums;

public enum HttpUrl {
    ROOT("/"),
    INDEX_URL("/index.html"),
    SIGN_UP("/user/signup"),
    LOGIN("/user/login"),
    LOGIN_FAILED("/user/login_failed.html"),
    LIST("/user/list.html"),
    USER_LIST("/user/userList");

    private final String path;

    HttpUrl(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }


}
