package org.ovirt.engine.core.utils.ovf;

@SuppressWarnings("serial")
public class OvfReaderException extends Exception {
    private String name;

    public OvfReaderException(String message, Exception ex, String name) {
        super(message, ex);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
