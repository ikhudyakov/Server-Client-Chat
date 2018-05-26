package components;

import java.io.Serializable;

public class Checker implements Serializable {
    private String check;

    public Checker(String check) {
        this.check = check;
    }

    public String getCheck() {
        return check;
    }
}
