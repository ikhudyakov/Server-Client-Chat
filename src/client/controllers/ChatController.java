package client.controllers;

import client.ChatClient;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ChatController {
    @FXML
    private TextField text_field;
    @FXML
    private ImageView immageButtonAttachFile;
    @FXML
    private ListView<String> messagesList;
    @FXML
    private Button sendButton;
    @FXML
    private ListView<String> onlineUsers;
    @FXML
    private Label login_label;
    private String text;
    private ChatClient chatClient;
    private String currentLogin;

    ObservableList<String> chatMessages = FXCollections.observableArrayList();
    ObservableList<String> loginOfUser = FXCollections.observableArrayList();


    @FXML
    void initialize() {
        chatClient = Controller.getChatClient();
        currentLogin = chatClient.getName();
        login_label.setText(currentLogin);
        onlineUsers.setItems(loginOfUser);
        messagesList.setItems(chatMessages);

        sendButton.setOnAction(event -> {
            sendMes();
        });

        text_field.setOnAction(event -> {
            sendMes();
        });

        Platform.runLater(
                () -> {
                    if (chatClient.getCheckMes()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        text = chatClient.getText();
                        chatMessages.add(text);
                        text = null;
                        chatClient.setCheckMes(false);

                    }
                }
        );

        new Thread(new MessageListener()).start();
    }


    private void sendMes() {
        //chatMessages.add( "USER " + text_field.getText());
        chatClient.sendText(text_field.getText());
        text_field.setText("");

        messagesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {

                System.out.println("ListView Selection Changed (newValue: " + newValue + ")\n");
            }
        });
    }

    private class MessageListener implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (chatClient.getCheckMes()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    text = chatClient.getText();
                    chatMessages.add(text);
                    text = null;
                    chatClient.setCheckMes(false);

                }
            }
        }
    }
}

