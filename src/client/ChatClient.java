package client;
import components.*;
import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;


public class ChatClient {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");    // формат времени
    private SocketAddress serverAddress;    // канал связи
    private String name;
    private Scanner scanner;
    private Socket socket;
    private ObjectOutputStream objOut;
    private int idChatRoom = 0;
    enum ClientState { CONNECTED,
                        LOGGED_IN
    }
    private ClientState clientState;

    private ChatClient(SocketAddress serverAddress, Scanner scanner) {
        this.serverAddress = serverAddress;
        this.scanner = scanner;


    }

    private void start() throws IOException, InterruptedException {

        openConnection();

        Thread reader = new Thread(new Reader(socket));
        reader.start();

        System.out.println(clientState);
        String msg;


        while (true){
            Thread.sleep(1000);
            if(clientState == ClientState.LOGGED_IN)
                break;
            String pass;
            System.out.println("Enter LOGIN");
            name = scanner.nextLine();
            msg = name;
            System.out.println("Enter PASSWORD");
            pass = scanner.nextLine();
            msg += " " + pass;
            buildAndSendMessage(msg);
        }
        System.out.println("Enter message to send: ");

        while (true) {
            msg = scanner.nextLine();
            if (msg.equals(("//"))){
                System.out.printf("All Commands:\n//newroom - Crate new chatroom\n//exit - Exit\n//switchchatroom");
            } else if(msg.equals("//newroom")){
                System.out.printf("enter the users you want to add to chatroom\n" +
                        "to stop, enter \"//s\"\n");
                List<String> users = new ArrayList<>();
                while (true){
                    msg = scanner.nextLine();
                    if (msg.equals("//s"))
                        break;
                    users.add(msg);
                }
                System.out.println(Arrays.toString(users.toArray()));
                buildAndSendMessage(users);
            } else if (msg.equals(("//switchchatroom"))){
                System.out.println("enter id chatroom");
                msg = scanner.nextLine();
                idChatRoom = Integer.parseInt(msg);
            } else if (msg != null && !msg.isEmpty())
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
        }

        catch (IOException e) {
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

                    Messages messages = (Messages)objIn.readObject();
                    if(messages instanceof Status){
                        Status status = (Status) messages;
                        switch (status.getStatusCode()){
                            case 1:
                                System.out.println("Success");
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
//                            case 4:
//                                System.out.println("user already logged on");
//                                clientState = ClientState.CONNECTED;
//                                break;
                            case 5:
                                System.out.printf("created ChatRoom with %s ID: %d\n", Arrays.toString(status.getUsers().toArray()), status.getIdChatRoom());
                                break;
                        }
                    } else if(messages instanceof TextMessage){
                        printMessage((TextMessage)messages);
                    }
                }
            }
            catch (IOException e) {

                throw new ChatUncheckedException("Error reading components", e);
            }
            catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            }
            finally {
                IOUtils.closeQuietly(socket);
                System.exit(1);
            }
        }
    }


    private void printMessage(TextMessage msg) {
        System.out.printf("[%s] from %s : %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    private void buildAndSendMessage (List users){
        Messages messages = null;
        messages = new ChatRoom(users);
        try {
            objOut.writeObject(messages);
            objOut.flush();
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    private void buildAndSendMessage(String msg) {
        Messages messages = null;

        if (clientState == ClientState.LOGGED_IN) {
            messages = new TextMessage(idChatRoom, System.currentTimeMillis(), name, msg);
        }
        else if (clientState == ClientState.CONNECTED){
            messages = new LoginCommand(msg);
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