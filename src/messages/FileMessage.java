package messages;

import java.io.File;
import java.io.Serializable;

public class FileMessage implements Messages, Serializable {

    private long timestamp;
    private String sender;
    private File file;
    private int id;

    public FileMessage(int id, long timestamp, String sender, File file) {
        this.id = id;
        this.timestamp = timestamp;
        this.sender = sender;
        this.file = file;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public File getFile() {
        return file;
    }

    public void setFile(String text) {
        this.file = file;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
