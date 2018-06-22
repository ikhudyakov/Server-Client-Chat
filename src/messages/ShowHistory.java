package messages;

import java.io.Serializable;
import java.util.List;

public class ShowHistory implements Messages, Serializable {
    private int idChatRoom;
    //private StringBuilder text;
    private List<String> text;
    private String login;

    public ShowHistory(int idChatRoom, String login){
        this.idChatRoom = idChatRoom;
        this.login = login;
    }

    public ShowHistory(int idChatRoom, String login, List<String> text){
        this.idChatRoom = idChatRoom;
        this.text = text;
        this.login = login;
    }

    public int getIdChatRoom() {
        return idChatRoom;
    }

    public List<String> getText() {
        return text;
    }

    public String getLogin() {
        return login;
    }

    public void setText(List<String> text) {
        this.text = text;
    }
}
