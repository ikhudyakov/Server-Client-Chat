package server;
import components.*;
import messages.LoginCommand;
import messages.Messages;
import messages.Status;
import messages.TextMessage;
import components.Connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatServer {

    private int port;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
    private String time = FORMAT.format(System.currentTimeMillis());
    private Map<String, Connection> userConnection = new ConcurrentHashMap<>();
    private List<String> onlineUsers = new ArrayList<>();
    private final BlockingDeque<Messages> messageQueue = new LinkedBlockingDeque<>();
    private byte [] header = {(byte) 0xAA, (byte) 0xAA};
    private Map<String, String> accMap = new HashMap<>();


    private ChatServer(int port) {
        this.port = port;
        accMap.put("user1", "pass1");
        accMap.put("user2", "pass2");
        accMap.put("user3", "pass3");
    }

    private void start() throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on " + serverSocket);

            while (true) {

                Socket sock = serverSocket.accept();
                InputStream in = sock.getInputStream();
                byte[] buf = new byte[2];
                int read = in.read(buf);
                while (read < 2 && read != -1) {
                    read += in.read(buf, read, buf.length - read);
                }

                if (Arrays.equals(buf, header)) {
                    Connection con = new Connection(sock);      // Создаем новое соединение с подключенным к серверу клиентом
                    System.out.println("success connection");
                    new Thread(new Reader(con)).start();
                }
                else {
                    System.out.println("Wrong header: " + Arrays.toString(buf));
                }
                new Thread(new Writer()).start();
            }
        }
    }

    private class Reader implements Runnable {
        private final Connection con;

        private Reader(Connection con) {
            this.con = con;
        }

        @Override
        public void run() {
            ObjectInputStream objIn;
            Status status;
            String login = null;
            try {
                objIn = new ObjectInputStream(con.socket.getInputStream());
                System.out.printf(time + " %s connected\n", con.socket.getInetAddress().getHostAddress());

                while (!Thread.currentThread().isInterrupted()) {

                    Messages messages = (Messages)objIn.readObject();
                    if(messages instanceof LoginCommand){                           // Проверка на принадлежность message к классу LoginCommand
                        LoginCommand loginCommand = (LoginCommand) messages;
                        login = loginCommand.getLogin();
                        userConnection.put(login, con);
                        if(accMap.containsKey(login)){                              // Содержит ли Мар полученный логин
                            String password = accMap.get(loginCommand.getLogin());
                            if(password.equals(loginCommand.getPassword())){        // Сравниваем взятый из Мар пароль с полученным от клиента
                                if(!onlineUsers.contains(login)) {
                                    onlineUsers.add(login);
                                    status = new Status(1, login);
                                    for (Map.Entry entry : userConnection.entrySet()) {
                                        if (userConnection.get(entry.getKey()).equals(con)) {
                                            userConnection.remove(entry.getKey());
                                        }
                                    }
                                    userConnection.put(login, con);
                                } else {
                                    status = new Status(4, login);
                                }
                            } else{
                                status = new Status(3, login);
                            }
                        } else {
                            status = new Status(2, login);
                        }
                        switch (status.getStatusCode()){
                            case 1:
                                System.out.println(time + " Success " + con.socket.getInetAddress().getHostAddress());
                                break;
                            case 2:
                                System.out.println(time + " incorrect login " + con.socket.getInetAddress().getHostAddress());
                                break;
                            case 3:
                                System.out.println(time + " incorrect password " + con.socket.getInetAddress().getHostAddress());
                                break;
                            case 4:
                                System.out.println(time + " user already logged on " + con.socket.getInetAddress().getHostAddress());
                                break;
                        }

                        messageQueue.add(status);

                    } else if(messages instanceof TextMessage){
                        if (((TextMessage) messages).getText().startsWith("//")){
                            if(((TextMessage) messages).getText().toLowerCase().contains("newroom")){
                                System.out.println("Enter room's name");

                            }
                        }
                        messageQueue.add(messages);
                        printMessage((TextMessage)messages);
                    }
                }
            }
            catch (IOException e) {
                //e.printStackTrace();
                System.err.println(time + " Disconnected " + con.socket.getInetAddress().getHostAddress());
            }
            catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            }
            finally {       // выполнится в любом случае

                if (login != null && userConnection.containsKey(login))
                    userConnection.remove(login);   // удаляет пользователя из Map
                if(onlineUsers.contains(login))
                    onlineUsers.remove(login);      //
                IOUtils.closeQuietly(con.socket);
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

                    if(msg instanceof Status){
                        Status msgOut = (Status) msg;

                        String login = msgOut.getLogin();
                        Connection connection = userConnection.get(login);
                        try{
                            connection.objOut.writeObject(msgOut);
                            connection.objOut.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                            userConnection.remove(login);
                            IOUtils.closeQuietly(connection.socket);
                        }

                    } else if(msg instanceof TextMessage) {
                        TextMessage msgOut = (TextMessage) msg;

                        for (Map.Entry entry: userConnection.entrySet()){
                            Connection connection = (Connection)entry.getValue();
                            try {
                                connection.objOut.writeObject(msgOut);
                                connection.objOut.flush();
                            } catch (IOException e) {
                                System.err.printf("Error sending components %s to %s\n", msg, connection.socket);
                                userConnection.remove(entry);
                                IOUtils.closeQuietly(connection.socket);
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                throw new ChatUncheckedException("Writer was interrupted", e);
            }
        }
    }



    private void printMessage(TextMessage msg) {
        System.out.printf("%s from %s : %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Port must be specified");
        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}