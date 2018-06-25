package client.controllers;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ResourceBundle;

import client.ChatClient;
import components.IOUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

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

    ChatClient chatClient;

    @FXML
    void initialize() {
        chatClient = Controller.getChatClient();



        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                IOUtils.closeQuietly(chatClient.getSocket());
                System.exit(0);
            }
        });

    }
}

