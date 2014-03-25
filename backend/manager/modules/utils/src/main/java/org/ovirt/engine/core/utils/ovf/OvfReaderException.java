package org.ovirt.engine.core.utils.ovf;

@SuppressWarnings("serial")
public class OvfReaderException extends Exception {
    private String name;

    public OvfReaderException(Exception ex, String name) {
        super(ex.getMessage(), ex);
        this.name = name == null ? OvfReader.EmptyName : name;
    }

    public OvfReaderException(Exception e) {
        super(e.getMessage(), e);
        this.name = OvfReader.EmptyName;
    }

    public String getName() {
        return name;
    }
}
