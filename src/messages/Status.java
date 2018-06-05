package messages;

import java.io.Serializable;
import java.util.List;

public class Status implements Messages, Serializable{
    private int statusCode;
    private String login;
    private List users;
    private int idChatRoom;

    public Status(int statusCode, String login) {
        this.statusCode = statusCode;
        this.login = login;
    }
    public Status(int statusCode, List users, int idChatRoom) {
        this.statusCode = statusCode;
        this.users = users;
        this.idChatRoom = idChatRoom;

    }

    public List getUsers() {
        return users;
    }

    public int getIdChatRoom() {
        return idChatRoom;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLogin() {
        return login;
    }
}
