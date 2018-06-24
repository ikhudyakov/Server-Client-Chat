package client;

import client.controllers.Controller;
import components.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import messages.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;


public class ChatClient {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");    // формат времени
    private SocketAddress serverAddress;    // канал связи
    private String name;
    private Scanner scanner;
    private Socket socket;
    private ObjectOutputStream objOut;
    private int idChatRoom = 0;
    private List<Integer> allId;
    private String msg;
    private boolean checkAuth = false;
    Controller controller;

    enum ClientState {
        CONNECTED,
        LOGGED_IN,
        REGISTERED
    }

    private ClientState clientState;

    public ChatClient(SocketAddress serverAddress, Scanner scanner) {
        this.serverAddress = serverAddress;
        this.scanner = scanner;
        allId = new ArrayList<>();
    }

    public void start() throws IOException, InterruptedException {

        openConnection();

        Thread reader = new Thread(new Reader(socket));
        reader.start();
        controller.enterButton.setOnAction(event -> {

                    String msg;
                    System.out.println("authentication");
                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (clientState == ClientState.LOGGED_IN)
                            break;
                        //System.out.println("Enter LOGIN");
                        //name = scanner.nextLine().trim().toLowerCase();

                        String login = controller.login_field.getText().toLowerCase().trim();
                        String password = controller.password_field.getText().toLowerCase().trim();
                        if (!login.equals("") && !password.equals("")) {
                            msg = login + " " + password;
                            buildAndSendMessage(msg);
                        }
                    }
                    if (clientState == ClientState.LOGGED_IN){
                        controller.enterButton.getScene().getWindow().hide();

                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/chat.fxml"));

                        try {
                            loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Parent root = loader.getRoot();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.showAndWait();
                    }
                }
            );

        controller.registrationButton.setOnAction(event -> {
            controller.registrationButton.getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/registration.fxml"));

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        });
//        System.out.println("1 - sign up\n2 - log in");
//        while (true) {
//            msg = scanner.nextLine();
//            if (msg.equals("1")) {
//                registration();
//                authentication();
//                break;
//            } else if (msg.equals("2")) {
//                authentication();
//                break;
//            } else System.out.println("error");
//        }
        showAllCommands();
        System.out.println("Enter message to send: ");
        textScanner();
    }

    private void registration() throws InterruptedException {
        clientState = ClientState.REGISTERED;
        System.out.println("registration");
        while (true) {
            Thread.sleep(1000);
            if (clientState == ClientState.CONNECTED)
                break;
            System.out.println("Enter LOGIN");
            name = scanner.nextLine().trim().toLowerCase();
            while (name.equals("")) {
                name = scanner.nextLine().trim().toLowerCase();
            }
            msg = name;
            System.out.println("Enter PASSWORD");
            String pass = scanner.nextLine().trim().toLowerCase();
            while (pass.equals("")) {
                pass = scanner.nextLine().trim().toLowerCase();
            }
            msg += " " + pass;
            buildAndSendMessage(msg);
        }
    }

    private void textScanner() {
        while (!Thread.currentThread().isInterrupted()) {
            msg = scanner.nextLine().trim().toLowerCase();
            if (msg.equals("//")) {
                showAllCommands();
            } else if (msg.equals("//newroom")) {
                System.out.print("enter the users you want to add to chatroom\n" +
                        "to stop, enter \"//s\"\n");
                List<String> users = new ArrayList<>();
                users.add(name);
                while (true) {
                    msg = scanner.nextLine();
                    if (msg.equals("//s"))
                        break;
                    users.add(msg);
                }
                buildAndSendMessage(users);
            } else if (msg.equals("//switchroom")) {
                System.out.println("enter id chatroom");
                msg = scanner.nextLine();
                if (allId.contains(Integer.parseInt(msg))) {
                    idChatRoom = Integer.parseInt(msg);
                    //buildAndSendMessage(idChatRoom, name);
                } else {
                    System.out.println("Error room ID");
                }
            } else if (msg.equals("//allroom")) {
                System.out.println(Arrays.toString(allId.toArray()));
            } else if (msg.equals("//exit")) {
                IOUtils.closeQuietly(socket);
            } else if (msg.equals("//sendfile")) {
                System.out.println("enter path file");
                String path = scanner.nextLine();
                sendFile(path);
            } else if (msg.equals("//showhistory")) {
                buildAndSendMessage(idChatRoom, name);
            } else if (!msg.isEmpty())
                buildAndSendMessage(msg);
        }
    }

    private void sendFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            buildAndSendMessage(file);
        } else System.out.println("Error path");
    }

    private void showAllCommands() {
        System.out.print("All Commands:\n// - Show all commands\n" +
                "//newroom - Crate new chatroom\n//exit - Exit\n" +
                "//switchroom - Switch chat room(all users ID: 0)\n" +
                "//allroom - Show all chat room\n//sendfile - Send file (path)\n" +
                "//showhistory - Show history chat room\n");
    }

    private void authentication() throws InterruptedException {
        String msg;
        System.out.println("authentication");
        while (true) {
            Thread.sleep(1000);
            if (clientState == ClientState.LOGGED_IN)
                break;
            System.out.println("Enter LOGIN");
            name = scanner.nextLine().trim().toLowerCase();
            while (name.equals("")) {
                name = scanner.nextLine().trim().toLowerCase();
            }
            msg = name;
            System.out.println("Enter PASSWORD");
            String pass = scanner.nextLine().trim().toLowerCase();
            while (pass.equals("")) {
                pass = scanner.nextLine().trim().toLowerCase();
            }
            msg += " " + pass;
            buildAndSendMessage(msg);
        }
    }


    private void openConnection() {
        try {
            socket = new Socket();
            socket.connect(serverAddress);
            clientState = ClientState.CONNECTED;
            byte[] header = {(byte) 0xAA, (byte) 0xAA};
            OutputStream out = socket.getOutputStream();
            out.write(header);
            System.out.println("Start socket");
            objOut = new ObjectOutputStream(out);
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);
            throw new ChatUncheckedException("Error connecting to server", e);
        }
    }

    private class Reader implements Runnable {
        private final Socket socket;

        private Reader(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                while (!Thread.currentThread().isInterrupted()) {

                    Messages messages = (Messages) objIn.readObject();
                    if (messages instanceof Status) {
                        Status status = (Status) messages;
                        if (!allId.contains(status.getIdChatRoom())) {
                            allId.add(status.getIdChatRoom());
                        }
                        switch (status.getStatusCode()) {
                            case 1:
                                System.out.println("Success");
                                allId = status.getAllId();
                                allId.add(0);
                                clientState = ClientState.LOGGED_IN;
                                checkAuth = true;
                                break;
                            case 2:
                                System.out.println("incorrect login");
                                clientState = ClientState.CONNECTED;
                                break;
                            case 3:
                                System.out.println("incorrect password");
                                clientState = ClientState.CONNECTED;
                                break;
                            case 5:
                                System.out.printf("created ChatRoom with %s ID: %d\n", Arrays.toString(status.getUsers().toArray()), status.getIdChatRoom());
                                break;
                            case 6:
                                System.out.println("Successful authentication");
                                clientState = ClientState.CONNECTED;
                                break;
                            case 7:
                                System.out.printf("login [%s] already exists\n", status.getLogin());
                                break;
                        }
                    } else if (messages instanceof TextMessage) {
                        if (((TextMessage) messages).getId() == idChatRoom)
                            printMessage((TextMessage) messages);
                    } else if (messages instanceof ShowHistory) {
                        for (String text : ((ShowHistory) messages).getText()) {
                            System.out.println(text);
                        }
                    } else if (messages instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) messages;
                        File file = fileMessage.getFile();
                        File file1 = new File("\\" + file.getName());
                        Files.copy(file.toPath(), file1.toPath());
                        //file.renameTo(new File("C:\\Users\\ily-k\\Desktop\\test\\" + file.getName()));
                        System.out.println("you have received the file " + file.getName());
                        //file.delete();
                    }
                }
            } catch (IOException e) {

                throw new ChatUncheckedException("Error reading components", e);
            } catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            } finally {
                IOUtils.closeQuietly(socket);
                System.exit(1);
                System.out.println("Socket closed");
            }
        }
    }


    private void printMessage(TextMessage msg) {
        System.out.printf("[%s] from %s : %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    private void buildAndSendMessage(List users) {
        Messages messages;
        messages = new ChatRoom(users);
        try {
            objOut.writeObject(messages);
            objOut.flush();
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    private void buildAndSendMessage(int idChatRoom, String login) {
        ShowHistory showHistory = new ShowHistory(idChatRoom, login);
        try {
            objOut.writeObject(showHistory);
            objOut.flush();
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    private void buildAndSendMessage(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Messages messages = new FileMessage(idChatRoom, System.currentTimeMillis(), name, file);

        try {
            objOut.writeObject(messages);
            objOut.flush();
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    public void buildAndSendMessage(String msg) {
        Messages messages = null;

        if (clientState == ClientState.REGISTERED) {
            messages = new Registration(msg);
        } else if (clientState == ClientState.LOGGED_IN) {
            messages = new TextMessage(idChatRoom, System.currentTimeMillis(), name, msg);
        } else if (clientState == ClientState.CONNECTED) {
            messages = new Authentication(msg);
        }
        try {
            objOut.writeObject(messages);
            objOut.flush();
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    public static SocketAddress parseAddress(String addr) {
        String[] split = addr.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }


}


