package components;

import java.io.Closeable;
import java.io.IOException;

public final class IOUtils {
    private IOUtils() {}

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}