public class MyException extends Exception {
    private String message;

    public MyException(String x) {
        message = x;
    }

    public String getMessage() {
        return message;
    }
}
