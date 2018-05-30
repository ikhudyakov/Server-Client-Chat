package messages;

public class LoginCommand implements Commands {
    String login;
    String password;

    public LoginCommand(String msg) {
        split(msg);
    }

    private void split(String msg){
        String[] strings = msg.split(" ");
        this.login = strings[0];
        this.password = strings[1];
    }


    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
