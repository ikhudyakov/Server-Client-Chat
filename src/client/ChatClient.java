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
import java.util.Date;
import java.util.Scanner;


public class ChatClient {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private SocketAddress serverAddress;
    private String name;
    private String password;
    private Scanner scanner;
    private Socket socket;
    private ObjectOutputStream objOut;
    private boolean check;
    enum ClientState { CONNECTED,
                        LOGGED_IN
    }
    private ClientState clientState;

    private ChatClient(SocketAddress serverAddress, Scanner scanner) {
        this.serverAddress = serverAddress;
        this.scanner = scanner;
        clientState = ClientState.CONNECTED;

    }

    private void start() throws IOException {

        openConnection();


//        System.out.println("Enter your login: ");
//        name = scanner.nextLine();



        Thread reader = new Thread(new Reader(socket));
        reader.start();

        System.out.println("Enter message to send: ");

        while (true) {
            String msg = scanner.nextLine();

            /*if ("/exit".equals(msg)) {
                IOUtils.closeQuietly(socket);

                break;
            }
            else if ("/nick".equals(msg)) {
                System.out.println("Enter new name:");

                name = scanner.nextLine();

                continue;
            }*/

            if (msg != null && !msg.isEmpty())
                buildAndSendMessage(msg);
        }
    }


    private void openConnection() {
        try {
            socket = new Socket();
            socket.connect(serverAddress);

            byte[] header = {(byte) 0xAA, (byte) 0xAA};
            OutputStream out = socket.getOutputStream();
            out.write(header);
            System.out.println("Start socket");
            objOut = new ObjectOutputStream(socket.getOutputStream());


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
                            case 2:
                                System.out.println("incorrect login");
                                clientState = ClientState.CONNECTED;
                            case 3:
                                System.out.println("incorrect password");
                                clientState = ClientState.CONNECTED;
                        }
                    } else if(messages instanceof TextMessage){
                        printMessage((TextMessage)messages);
                    }

//                    TextMessage message = (TextMessage) objIn.readObject();
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
        System.out.printf("%s: %s => %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    private void buildAndSendMessage(String msg) {
        Messages messages = null;

        if (clientState == ClientState.LOGGED_IN) {
            messages = new TextMessage(System.currentTimeMillis(), name, msg);
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

    public static void main(String[] args) throws IOException {
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