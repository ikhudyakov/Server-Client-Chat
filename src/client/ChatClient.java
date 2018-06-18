package client;

import components.*;
import javafx.application.Application;
import messages.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class ChatClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("ChatTick");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");    // формат времени
    private SocketAddress serverAddress;    // канал связи
    private String name;
    private Scanner scanner;
    private Socket socket;
    private ObjectOutputStream objOut;
    private int idChatRoom = 0;
    private List<Integer> allId;
    private String msg;

    enum ClientState {
        CONNECTED,
        LOGGED_IN,
        REGISTRATION
    }

    private ClientState clientState;

    private ChatClient(SocketAddress serverAddress, Scanner scanner) {
        this.serverAddress = serverAddress;
        this.scanner = scanner;
        allId = new ArrayList<>();
    }

    private void start() throws IOException, InterruptedException {

        openConnection();

        Thread reader = new Thread(new Reader(socket));
        reader.start();

        System.out.println("1 - sign up\n2 - log in");
        msg = scanner.nextLine();
        switch (msg) {
            case "1":
                registration();
                authentication();
                break;
            case "2":
                authentication();
                break;
            default:
                System.out.println("error");
                break;
        }
        showAllCommands();
        System.out.println("Enter message to send: ");
        textScanner();
    }

    private void registration() throws InterruptedException {
        clientState = ClientState.REGISTRATION;
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
                        System.out.println(((ShowHistory) messages).getText());
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

    private void buildAndSendMessage(String msg) {
        Messages messages = null;

        if (clientState == ClientState.REGISTRATION) {
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

    private static SocketAddress parseAddress(String addr) {
        String[] split = addr.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        //launch(args);
        //ChatServer.main(8080);
        start(args);
        //return;
    }

    private static void start(String[] args) throws IOException, InterruptedException {
        String address = null;

        if (args != null && args.length > 0)
            address = args[0];

        Scanner scanner = new Scanner(System.in);

        if (address == null) {
            System.out.println("Enter server address");
            address = scanner.nextLine();
        }

        ChatClient client = new ChatClient(parseAddress(address), scanner);
        client.start();
    }
}