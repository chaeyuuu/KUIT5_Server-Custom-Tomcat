package http.enums;

public enum HttpStatus {
    OK ("200 OK"),
    FOUND("302 Found");

    private final String status;

    HttpStatus(String status) {
        this.status=status;
    }

    public String getStatus(){  
        return "HTTP/1.1 " + status + "\r\n";
    }
}
