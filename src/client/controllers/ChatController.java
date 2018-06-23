package client.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ChatController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField text_field;

    @FXML
    private ImageView immageButtonAttachFile;

    @FXML
    private ListView<?> messagesList;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<?> onlineUsers;

    @FXML
    void initialize() {

    }
}

