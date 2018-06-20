package server;

import components.*;
import messages.*;
import components.Connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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
    private byte[] header = {(byte) 0xAA, (byte) 0xAA};
    //private Map<String, String> accMap = new HashMap<>();
    private ChatRoom chat;
    private static int incId;


    private ChatServer(int port) {
        chat = new ChatRoom();
        chatRoomList.add(0, chat);
        this.port = port;
    }

    private void getChatRooms() {
        int max;
        try (java.sql.Connection JDBCConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chatdb",
               "admin", "1qaz2wsx")) {
            PreparedStatement prepared = JDBCConnection.prepareStatement("SELECT id_room FROM chatrooms");
            try (ResultSet rs = prepared.executeQuery()) {
                if (rs.next()) {
                    while (rs.next()) {
                        max = rs.getInt("id_room");
                        if (max > incId) {
                            incId = max;
                        }
                    }
                    int i;
                    for (i = 1; i <= incId; i++) {
                        prepared = JDBCConnection.prepareStatement("SELECT * FROM chatrooms where id_room=?");
                        prepared.setInt(1, i);
                        List<String> users = new ArrayList<>();
                        try (ResultSet rs1 = prepared.executeQuery()) {
                            while (rs1.next()) {
                                String login = rs1.getString("login");
                                users.add(login);
                            }
                            ChatRoom chatRoom = new ChatRoom(users);
                            chatRoom.setId(i);
                            chatRoomList.add(i, chatRoom);
                        }
                    }
                } else incId = 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        getChatRooms();
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
                } else {
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
            Status status = null;
            String login = null;
            String password;

            try {
                objIn = new ObjectInputStream(con.socket.getInputStream());
                System.out.printf("[%s] connected %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());

                Class.forName("org.postgresql.Driver");


                while (!Thread.currentThread().isInterrupted()) {
                    try (java.sql.Connection JDBCConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chatdb",
                            "admin", "1qaz2wsx")) {

                        Messages messages = (Messages) objIn.readObject();

                        if (messages instanceof Registration) {
                            Registration registration = (Registration) messages;
                            login = registration.getLogin();
                            password = registration.getPassword();
                            connectionMap.put(login, con);


                            PreparedStatement prepared = JDBCConnection.prepareStatement("SELECT * FROM users WHERE login=?");
                            prepared.setString(1, login);

                            try (ResultSet rs = prepared.executeQuery()) {
                                if (rs.next()) {
                                    String dbLogin = rs.getString("login");
                                    status = new Status(7, dbLogin);
                                } else {
                                    prepared = JDBCConnection.prepareStatement("INSERT INTO USERS (ID, LOGIN, PASSWORD) VALUES (nextval('iduser'),?,?)");
                                    prepared.setString(1, login);
                                    prepared.setString(2, password);
                                    prepared.executeUpdate();
                                    status = new Status(6, login);
                                }
                                switch (status.getStatusCode()) {
                                    case 6:
                                        System.out.printf("[%s] Successful authentication\n", FORMAT.format(System.currentTimeMillis()));
                                        break;
                                    case 7:
                                        System.out.printf("[%s] login [%s] already exists\n", FORMAT.format(System.currentTimeMillis()), login);
                                        break;
                                }
                                messageQueue.add(status);
                            }
                        }
/*
                        Работа с MAP

                        if (!accMap.containsKey(login)) {
                            accMap.put(login, password);
                            status = new Status(6, login);
                        } else {
                            status = new Status(7, login);
                        }
                        switch (status.getStatusCode()) {
                            case 6:
                                System.out.printf("[%s] Successful authentication\n", FORMAT.format(System.currentTimeMillis()));
                                break;
                            case 7:
                                System.out.printf("[%s] login [%s] already exists\n", FORMAT.format(System.currentTimeMillis()), login);
                                break;
                        }
                        messageQueue.add(status);
*/


                        if (messages instanceof Authentication) {                           // Проверка на принадлежность message к классу Authentication
                            Authentication authentication = (Authentication) messages;
                            login = authentication.getLogin();
                            password = authentication.getPassword();
                            connectionMap.put(login, con);

                            PreparedStatement prepared = JDBCConnection.prepareStatement("SELECT * FROM users WHERE login=?");
                            prepared.setString(1, login);
                            try (ResultSet rs = prepared.executeQuery()) {
                                if (rs.next()) {
                                    String dbLogin = rs.getString("login");
                                    prepared = JDBCConnection.prepareStatement("SELECT password FROM users WHERE login=?");
                                    prepared.setString(1, dbLogin);
                                    try (ResultSet rs1 = prepared.executeQuery()) {
                                        while (rs1.next()) {
                                            String dbPassword = rs.getString("password");
                                            if (dbPassword.equals(password)) {
                                                if (userConnection.containsKey(login)) {
                                                    System.out.printf("[%s] user with login \"%s\" was authorized\n", FORMAT.format(System.currentTimeMillis()), login);
                                                    userConnection.get(login).socket.close();
                                                }
                                                try {
                                                    Thread.sleep(500);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                prepared = JDBCConnection.prepareStatement("SELECT id_room FROM chatrooms WHERE login=?");
                                                prepared.setString(1, dbLogin);
                                                List<Integer> allId = new ArrayList<>();
                                                try (ResultSet rs2 = prepared.executeQuery()) {
                                                    while (rs2.next()) {
                                                        allId.add(rs2.getInt("id_room"));
                                                    }
                                                }
                                                status = new Status(1, dbLogin, allId);
                                                userConnection.put(dbLogin, con);
                                                if (!chat.getUsers().contains(dbLogin)) {
                                                    chat.setUsers(dbLogin);
                                                }
                                                if (chatRoomList.size() > 0) {
                                                    chatRoomList.remove(0);
                                                }
                                                chatRoomList.add(0, chat);
                                            } else status = new Status(3, login);
                                        }
                                    }
                                } else {
                                    status = new Status(2, login);
                                }
                                switch (status.getStatusCode()) {
                                    case 1:
                                        System.out.printf("[%s] Successful authentication %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                        break;
                                    case 2:
                                        System.out.printf("[%s] incorrect login %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                        break;
                                    case 3:
                                        System.out.printf("[%s] incorrect password %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                        break;
                                }
                                messageQueue.add(status);
                            }

                        }
/*
                            if (accMap.containsKey(login)) {                              // Содержит ли Мар полученный логин
                                password = accMap.get(login);
                                if (password.equals(authentication.getPassword())) {        // Сравниваем взятый из Мар пароль с полученным от клиента
                                    if (userConnection.containsKey(login)) {
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
                                    if (!chat.getUsers().contains(login)) {
                                        chat.setUsers(login);
                                    }
                                    if (chatRoomList.size() > 0) {
                                        chatRoomList.remove(0);
                                    }
                                    chatRoomList.add(0, chat);
                                } else {
                                    status = new Status(3, login);
                                }
                            } else {
                                status = new Status(2, login);
                            }
                            switch (status.getStatusCode()) {
                                case 1:
                                    System.out.printf("[%s] Successful authentication %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                    break;
                                case 2:
                                    System.out.printf("[%s] incorrect login %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                    break;
                                case 3:
                                    System.out.printf("[%s] incorrect password %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
                                    break;
                            }
                            messageQueue.add(status);

                        }*/


                        else if (messages instanceof ChatRoom) {

                            ChatRoom chatRoom = (ChatRoom) messages;
                            chatRoom.setId(++incId);
                            List<String> chatUser = chatRoom.getUsers();
                            for (String user : chatUser) {
                                PreparedStatement prepared = JDBCConnection.prepareStatement("INSERT INTO CHATROOMS (ID_ROOM, LOGIN) VALUES (?,?)");
                                prepared.setInt(1, chatRoom.getId());
                                prepared.setString(2, user);
                                prepared.executeUpdate();
                            }


                            chatRoomList.add(chatRoom);
                            messageQueue.add(chatRoom);
                            System.out.printf("[%s] Created ChatRoom with %s ID: %d\n", FORMAT.format(System.currentTimeMillis()), Arrays.toString(chatRoom.getUsers().toArray()), chatRoom.getId());

                        } else if (messages instanceof ShowHistory) {
                            ShowHistory msg = (ShowHistory) messages;
                            StringBuilder stringBuilder = new StringBuilder();
                            PreparedStatement prepared = JDBCConnection.prepareStatement("SELECT TEXT FROM HISTORY WHERE ID_ROOM=?");
                            prepared.setInt(1, msg.getIdChatRoom());
                            try (ResultSet rs = prepared.executeQuery()) {
                                while (rs.next()) {
                                    stringBuilder.append(rs.getString("text"));
                                }
                            }
                            msg.setText(stringBuilder);
                            messageQueue.add(messages);
                        } else if (messages instanceof TextMessage) {
                            messageQueue.add(messages);
                            printMessage((TextMessage) messages);
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.err.printf("[%s] Disconnected %s\n", FORMAT.format(System.currentTimeMillis()), con.socket.getInetAddress().getHostAddress());
            } catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {       // выполнится в любом случае

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

                    if (msg instanceof ChatRoom) {
                        ChatRoom chatRoom = (ChatRoom) msg;

                        Status msgOut = new Status(5, chatRoom.getUsers(), chatRoom.getId());
                        for (String user : chatRoom.getUsers()) {
                            Connection connection = connectionMap.get(user);
                            try {
                                connection.objOut.writeObject(msgOut);
                                connection.objOut.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                                connectionMap.remove(user);
                                IOUtils.closeQuietly(connection.socket);
                            }
                        }
                    } else if (msg instanceof Status) {
                        Status msgOut = (Status) msg;

                        String login = msgOut.getLogin();
                        Connection connection = connectionMap.get(login);
                        try {
                            connection.objOut.writeObject(msgOut);
                            connection.objOut.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                            connectionMap.remove(login);
                            IOUtils.closeQuietly(connection.socket);
                        }

                    } else if (msg instanceof TextMessage) {
                        TextMessage msgOut = (TextMessage) msg;
                        //History.saveMessageInFile(msgOut);
                        History.saveMessageInDB(msgOut);

                        for (ChatRoom list : chatRoomList) {
                            if (msgOut.getId() == list.getId()) {
                                List users = list.getUsers();
                                for (Object login : users) {
                                    Connection connection = userConnection.get(login);
                                    try {
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
                    } else if (msg instanceof ShowHistory) {
                        ShowHistory msgOut = (ShowHistory) msg;
                        String login = msgOut.getLogin();
                        Connection connection = userConnection.get(login);
                        try {
                            connection.objOut.writeObject(msgOut);
                            connection.objOut.flush();
                        } catch (IOException e) {
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);
                            userConnection.remove(login);
                            IOUtils.closeQuietly(connection.socket);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new ChatUncheckedException("Writer was interrupted", e);
            } //catch (IOException e) {
                //e.printStackTrace();
            //}
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