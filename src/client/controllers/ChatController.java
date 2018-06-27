package client.controllers;

import java.awt.*;
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

    @FXML private TextField text_field;
    @FXML private ImageView immageButtonAttachFile;
    @FXML private ListView<String> messagesList;
    @FXML private Button sendButton;
    @FXML private ListView<String> onlineUsers;
    @FXML private Label login_label;
    private String text;
    private ChatClient chatClient;
    private String currentLogin;

    ObservableList<String> chatMessages = FXCollections.observableArrayList();
    ObservableList<String> loginOfUser = FXCollections.observableArrayList();


    @FXML void initialize() {
        currentLogin = chatClient.getName();
        login_label.setText(currentLogin);
        chatClient = Controller.getChatClient();
        onlineUsers.setItems(loginOfUser);
        messagesList.setItems(chatMessages);

        sendButton.setOnAction(event -> {
            sendMes();
        });

        text_field.setOnAction(event -> {
            sendMes();
        });
    }

    private void sendMes() {
        //chatMessages.add( "USER " + text_field.getText());
        chatClient.sendText(text_field.getText());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        text = chatClient.getText();
        chatMessages.add(text);
        text_field.setText("");
    }
}

