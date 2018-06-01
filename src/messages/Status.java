package messages;

import java.io.Serializable;

public class Status implements Messages, Serializable{
    private int statusCode;
    private String login;

    public Status(int statusCode, String login) {
        this.statusCode = statusCode;
        this.login = login;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
