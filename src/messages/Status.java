package messages;

import java.io.Serializable;

public class Status implements Messages, Serializable{
    private int statusCode;

    public Status(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
