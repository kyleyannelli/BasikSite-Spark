package helpers;

import org.tinylog.Logger;

import java.io.IOException;
import java.io.Writer;

public class TinylogWriter extends Writer {
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        Logger.info(new String(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
