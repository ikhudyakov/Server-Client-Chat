package client.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField text_field;

    @FXML
    private Button sendButton;

    @FXML
    private Button attachButton;

    @FXML
    void initialize() {
        assert text_field != null : "fx:id=\"text_field\" was not injected: check your FXML file 'chat.fxml'.";
        assert sendButton != null : "fx:id=\"sendButton\" was not injected: check your FXML file 'chat.fxml'.";
        assert attachButton != null : "fx:id=\"attachButton\" was not injected: check your FXML file 'chat.fxml'.";

    }
}
