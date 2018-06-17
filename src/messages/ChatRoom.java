package messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Commands, Serializable {

    //    private static int incId = 0;
    private int id;
    private List<String> users;

    public ChatRoom(List<String> users) {
//        incId++;
        this.id = -1;
        this.users = users;
    }

    public ChatRoom() {
        this.id = 0;
        users = new ArrayList<>();
    }

    public void setUsers(String login) {
        this.users.add(login);
    }

    public int getId() {
        return id;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setId(int id) {
        this.id = id;
    }
}
