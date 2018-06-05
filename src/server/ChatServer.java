package server;
import components.*;
import messages.*;
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
    private Map<String, Connection> userConnection = new ConcurrentHashMap<>();
    private Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private List<ChatRoom> chatRoomList = new ArrayList<>();
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
            new Thread(new Writer()).start();

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
                System.out.printf("[%s] connected %s\n",FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());

                while (!Thread.currentThread().isInterrupted()) {

                    Messages messages = (Messages)objIn.readObject();
                    if(messages instanceof LoginCommand){                           // Проверка на принадлежность message к классу LoginCommand
                        LoginCommand loginCommand = (LoginCommand) messages;
                        login = loginCommand.getLogin();
                        connectionMap.put(login, con);

                        if(accMap.containsKey(login)){                              // Содержит ли Мар полученный логин
                            String password = accMap.get(login);
                            if(password.equals(loginCommand.getPassword())){        // Сравниваем взятый из Мар пароль с полученным от клиента
                                if(userConnection.containsKey(login)) {
                                    System.out.printf("[%s] user with login \"%s\" was authorized\n", FORMAT.format(System.currentTimeMillis()), login);
                                    userConnection.get(login).socket.close();
                                }
                                try {
                                    Thread.sleep(500);          // если усыпить поток, тогда вроде работает
                                } catch (InterruptedException e) {   // а без этого, блок finally видимо выполнялся после
                                    e.printStackTrace();             // того, как мы кладем новый логин и сокет userConnection.put(login, con);
                                }                                    // поэтому он удалял новые данные из мапы и сообзения опять не рассылались
                                status = new Status(1, login);
                                    userConnection.put(login, con);
                            } else{
                                status = new Status(3, login);
                            }
                        } else {
                            status = new Status(2, login);
                        }
                        switch (status.getStatusCode()) {
                            case 1:
                                System.out.printf("[%s] Success %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                break;
                            case 2:
                                System.out.printf("[%s] incorrect login %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                break;
                            case 3:
                                System.out.printf("[%s] incorrect password %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                break;
                        }
                        messageQueue.add(status);

                    } else if(messages instanceof ChatRoom){

                        ChatRoom chatRoom = (ChatRoom) messages;
                        chatRoomList.add(chatRoom);
                        messageQueue.add(chatRoom);
                        System.out.printf("[%s] Created ChatRoom with %s ID: %d\n", FORMAT.format(System.currentTimeMillis()), Arrays.toString(chatRoom.getUsers().toArray()), chatRoom.getId());

                    } else if(messages instanceof TextMessage){
                        messageQueue.add(messages);
                        printMessage((TextMessage)messages);
                    }
                }
            }
            catch (IOException e) {
                //e.printStackTrace();
                System.err.printf("[%s] Disconnected %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
            }
            catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            }
            finally {       // выполнится в любом случае

                if (login != null && userConnection.containsKey(login))
                    userConnection.remove(login);   // удаляет пользователя из Map
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

                    if(msg instanceof ChatRoom){
                        ChatRoom chatRoom = (ChatRoom) msg;

                        Status msgOut = new Status(5, chatRoom.getUsers(), chatRoom.getId());
                        for(String user : chatRoom.getUsers()){
                            Connection connection = connectionMap.get(user);
                            try{
                                connection.objOut.writeObject(msgOut);
                                connection.objOut.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                                connectionMap.remove(user);
                                IOUtils.closeQuietly(connection.socket);
                            }
                        }
                    }

                    if(msg instanceof Status){
                        Status msgOut = (Status) msg;

                        String login = msgOut.getLogin();
                        Connection connection = connectionMap.get(login);
                        try{
                            connection.objOut.writeObject(msgOut);
                            connection.objOut.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                            connectionMap.remove(login);
                            IOUtils.closeQuietly(connection.socket);
                        }

                    } else if(msg instanceof TextMessage) {
                        TextMessage msgOut = (TextMessage) msg;

                        if (msgOut.getId()!= 0){
                            for(ChatRoom list : chatRoomList){
                                if(msgOut.getId() == list.getId()) {
                                    List users = list.getUsers();
                                    for (Object login : users){
                                        Connection connection = userConnection.get(login);
                                        try {

                                            //TODO save history for ROOMS

                                            connection.objOut.writeObject(msgOut);
                                            connection.objOut.flush();
                                        } catch (IOException e) {
                                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);
                                            userConnection.remove(login);
                                            IOUtils.closeQuietly(connection.socket);
                                        }
                                    }
                                }
                            }
                        } else {
                            for (Map.Entry entry : userConnection.entrySet()) {
                                Connection connection = (Connection) entry.getValue();
                                try {

                                    //TODO save history for ALL

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
            }
            catch (InterruptedException e) {
                throw new ChatUncheckedException("Writer was interrupted", e);
            }
        }
    }

    private void printMessage(TextMessage msg) {
        System.out.printf("[%s] from %s : %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Port must be specified");
        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}