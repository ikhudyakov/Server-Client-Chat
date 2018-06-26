package client.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ResourceBundle;

import client.ChatClient;
import components.IOUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class ChatController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField text_field;
    @FXML private ImageView immageButtonAttachFile;
    @FXML private ListView<String> messagesList;
    @FXML private Button sendButton;
    @FXML private ListView<String> onlineUsers;

    ObservableList<String> chatMessages = FXCollections.observableArrayList();

    ChatClient chatClient;

    @FXML void initialize() {
        chatClient = Controller.getChatClient();

        messagesList.setItems(chatMessages);

        sendButton.setOnAction(event -> {
            chatMessages.add( chatClient + " " + text_field.getText());
            text_field.setText("");


//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                IOUtils.closeQuietly(chatClient.getSocket());
//                System.exit(0);
//            }
        });

    }
}

