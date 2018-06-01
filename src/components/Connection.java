package components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
    public final Socket socket;
    public final ObjectOutputStream objOut;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.objOut = new ObjectOutputStream(socket.getOutputStream());
//        this.objOut = new ObjectOutputStream(new ByteArrayOutputStream());
    }
}