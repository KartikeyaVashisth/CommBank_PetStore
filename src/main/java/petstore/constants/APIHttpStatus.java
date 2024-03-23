package petstore.constants;

public enum APIHttpStatus {

	OK_200(200, "OK"),
    BAD_REQUEST_400(400, "Bad Request"),
    UNAUTHORIZED_401(401, "Unauthorized"),
    NOT_FOUND_404(404, "Not Found"),
    INTERNAL_SERVER_ERROR_500(500, "Internal Server Error");

    private final int code;
    private final String message;

    APIHttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + " " + message;
    }
}
