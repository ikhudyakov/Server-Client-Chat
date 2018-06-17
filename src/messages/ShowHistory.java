package messages;

import java.io.Serializable;

public class ShowHistory implements Messages, Serializable {
    private int idChatRoom;
    private StringBuilder text;
    private String login;

    public ShowHistory(int idChatRoom, String login){
        this.idChatRoom = idChatRoom;
        this.login = login;
    }

    public ShowHistory(int idChatRoom, String login, StringBuilder text){
        this.idChatRoom = idChatRoom;
        this.text = text;
        this.login = login;
    }

    public int getIdChatRoom() {
        return idChatRoom;
    }

    public StringBuilder getText() {
        return text;
    }

    public String getLogin() {
        return login;
    }

    public void setText(StringBuilder text) {
        this.text = text;
    }
}
