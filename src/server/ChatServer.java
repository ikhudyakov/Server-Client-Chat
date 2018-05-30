package server;
import components.*;
import messages.LoginCommand;
import messages.Messages;
import messages.Status;
import messages.TextMessage;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatServer {

    private int port;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private final Set<Connection> connections = new CopyOnWriteArraySet<>();
    private final BlockingDeque<Messages> messageQueue = new LinkedBlockingDeque<>();
    byte [] header = {(byte) 0xAA, (byte) 0xAA};
    Map<String, String> accMap = new HashMap<>();


    public ChatServer(int port) {
        this.port = port;
        accMap.put("test1", "pass1");
        accMap.put("test2", "pass2");
        accMap.put("test3", "pass3");
    }

    private void start() throws IOException {



        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on " + serverSocket);

            while (true) {

                Socket sock = serverSocket.accept();
                Connection con = new Connection(sock);
                connections.add(con);
                InputStream in = sock.getInputStream();
                byte[] buf = new byte[2];
                int read = in.read(buf);
                while (read < 2 && read != -1) {
                    read = in.read(buf, read, buf.length - read);
                }

                if (Arrays.equals(buf, header)) {
                    System.out.println("success connection");
                    new Thread(new Reader(sock)).start();
                    new Thread(new Writer()).start();
                }
                else {
                    System.out.println("Wrong header: " + Arrays.toString(buf));
                }

                //


//                new Thread(new Writer()).start();
            }
        }

    }

    private class Reader implements Runnable {
        private final Socket socket;

        private Reader(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream objIn;
            Status status;



            try {
                objIn = new ObjectInputStream(socket.getInputStream());
                System.out.printf("%s connected\n", socket.getInetAddress().getHostAddress());

                while (!Thread.currentThread().isInterrupted()) {
//                    TextMessage msg = (TextMessage) objIn.readObject();

                    Messages messages = (Messages)objIn.readObject();
                    if(messages instanceof LoginCommand){                           // Проверка на принадлежность message к классу LoginCommand
                        LoginCommand loginCommand = (LoginCommand) messages;
                        if(accMap.containsKey(loginCommand.getLogin())){            // Содержит ли Мар полученный логин
                            String password = accMap.get(loginCommand.getLogin());
                            if(password.equals(loginCommand.getPassword())){             // Сравниваем взятый из Мар пароль с полученным от клиента
                                status = new Status(1);

                                System.out.println(status.getStatusCode());
                                messageQueue.add(status);
                            } else{
                                status = new Status(3);

                                System.out.println(status.getStatusCode());
                                messageQueue.add(status);
                            }
                        } else {
                            status = new Status(2);

                            System.out.println(status.getStatusCode());
                            messageQueue.add(status);
                        }
                    } else if(messages instanceof TextMessage){
                        messageQueue.add(messages);
                        printMessage((TextMessage)messages);
                    }



                }

            }
            catch (IOException e) {
                System.err.println("Disconnected " + socket.getInetAddress().getHostAddress());
            }
            catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            }
            finally {
                connections.removeIf(connection -> connection.socket == socket);
                IOUtils.closeQuietly(socket);
            }
        }
    }

    private class Writer implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("Writer");

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Messages msg = messageQueue.take();
                    Messages msgOut = null;
                    if(msg instanceof Status){
                        msgOut = (Status) msg;
                    } else if(msg instanceof TextMessage){
                        msgOut = (TextMessage) msg;
                    }

                    for (Connection connection : connections) {
                        try {
                            connection.objOut.writeObject(msgOut);
                            connection.objOut.flush();
                        }
                        catch (IOException e) {
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                            connections.remove(connection);
                            IOUtils.closeQuietly(connection.socket);
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                throw new ChatUncheckedException("Writer was interrupted", e);
            }
        }
    }

    private static class Connection {
        final Socket socket;
        final ObjectOutputStream objOut;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            this.objOut = new ObjectOutputStream(socket.getOutputStream());
        }
    }

    private void printMessage(TextMessage msg) {
        System.out.printf("%s: %s => %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Port must be specified");
        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}