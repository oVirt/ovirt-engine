package org.ovirt.engine.core.compat.backendcompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.ovirt.engine.core.compat.CompatException;

public class TextReaderCompat {

    protected BufferedReader content;

    public TextReaderCompat() {
    }

    public TextReaderCompat(Reader reader) {
        setReader(reader);
    }

    public void setReader(Reader reader) {
        content = new BufferedReader(reader);
    }

    public String ReadLine() {
        try {
            return content.readLine();
        } catch (IOException e) {
            throw new CompatException(e);
        }
    }

}
