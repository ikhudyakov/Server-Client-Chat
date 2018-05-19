package message;

public class ChatUncheckedException extends RuntimeException {
    public ChatUncheckedException(String message) {
        super(message);
    }

    public ChatUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}