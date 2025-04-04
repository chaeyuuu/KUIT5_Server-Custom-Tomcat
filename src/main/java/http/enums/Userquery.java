package http.enums;

public enum Userquery {
    USER_ID("userId"),
    PASSWORD("password"),
    NAME("name"),
    EMAIL("email");

    private final String query;

    Userquery(String query) {
        this.query = query;
    }

    public String getKey(){
        return query;
    }
}
