package client;
import components.*;

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

    public ChatClient(SocketAddress serverAddress, Scanner scanner) {
        this.serverAddress = serverAddress;
        this.scanner = scanner;
    }

    private void start() throws IOException {

        openConnection();

        System.out.println("Enter your login: ");
        name = scanner.nextLine();
        System.out.println("Enter your password: ");
        password = scanner.nextLine();


        buildAndSendAuth(name, password);
        System.out.println("Enter your name: ");

        name = scanner.nextLine();

        openConnection();

        Thread reader = new Thread(new Reader(socket));
        reader.start();

        System.out.println("Enter message to send: ");

        while (true) {
            String msg = scanner.nextLine();

            if ("/exit".equals(msg)) {
                IOUtils.closeQuietly(socket);

                break;
            }
            else if ("/nick".equals(msg)) {
                System.out.println("Enter new name:");

                name = scanner.nextLine();

                continue;
            }

            if (msg != null && !msg.isEmpty())
                buildAndSendMessage(msg);
        }
    }



    /*private void start() throws IOException {


        openConnection();

        Thread reader = new Thread(new Reader(socket));
        reader.start();

        while (true) {
            //new Thread(new ReaderAuth(socket)).start();
            System.out.println("Enter your login: ");
            name = scanner.nextLine();
            System.out.println("Enter your password: ");
            password = scanner.nextLine();
            buildAndSendAuth(name, password);
            if (check) break;
        }

        Thread readerAuth = new Thread(new ReaderAuth(socket));
        Thread reader = new Thread(new Reader(socket));
        readerAuth.start();


        reader.start();

        //System.out.println("Enter components to send: ");

        /*while (true) {
            String msg = scanner.nextLine();

            if ("/exit".equals(msg)) {
                IOUtils.closeQuietly(socket);
                break;
            }
            else if ("/nick".equals(msg)) {
                System.out.println("Enter new name:");
                name = scanner.nextLine();

                continue;
            }

            if (msg != null && !msg.isEmpty())
                buildAndSendMessage(msg);
        }*/


    private void openConnection() {
        try {
            socket = new Socket();
            socket.connect(serverAddress);
            objOut = new ObjectOutputStream(socket.getOutputStream());
            //objOutAuth = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Start socket");
            byte[] header = {1, 1};
            OutputStream out = socket.getOutputStream();
            out.write(header);
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
                    Message message = (Message) objIn.readObject();
                    printMessage(message);
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

    private class ReaderAuth implements Runnable {
        private final Socket socket;
        private ReaderAuth(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());

                while (!Thread.currentThread().isInterrupted()) {
                    check = (boolean) objIn.readObject();
                    System.out.println(check);
                    //if(check) Thread.currentThread().interrupt();
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

    private void printMessage(Message msg) {
        System.out.printf("%s: %s => %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    private void buildAndSendAuth(String name, String password) {
        Registration registration = new Registration(name, password);
        Thread thread = new Thread(new ReaderAuth(socket));
        thread.start();

        try {
            objOut.writeObject(registration);
            objOut.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
            IOUtils.closeQuietly(socket);

            throw new ChatUncheckedException("Error sending components", e);
        }
    }

    private void buildAndSendMessage(String msg) {
        Message message = new Message(System.currentTimeMillis(), name, msg);

        try {
            objOut.writeObject(message);
            objOut.flush();
        }
        catch (IOException e) {
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