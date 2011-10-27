package org.ovirt.engine.core.compat.backendcompat;

import java.io.IOException;
import java.io.Reader;
import org.ovirt.engine.core.compat.CompatException;

public class StreamReaderCompat extends TextReaderCompat {

    public StreamReaderCompat(Reader content) {
        super(content);
    }

    public StreamReaderCompat(String path) {
        super();
        try {
            setReader(new java.io.FileReader(path));
        } catch (java.io.FileNotFoundException e) {
            throw new CompatException(e);
        }
    }

    public String ReadToEnd() {
        StringBuilder output = new StringBuilder();
        try {
            char[] nextChar = new char[1];
            int success = 0;
            do {
                success = content.read(nextChar);
                if (success != -1)
                    output.append(nextChar);
            } while (success != -1);
        } catch (IOException e) {
            throw new CompatException(e);
        }
        return output.toString();
    }

    public void Dispose() {
        try {
            content.close();
        } catch (IOException e) {
            throw new CompatException(e);
        }
    }

    public void dispose() {
        this.Dispose();
    }
}
