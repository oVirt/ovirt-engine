package org.ovirt.engine.api.rsdl;

public class Argument {

    public Argument(String name, String type) {
        this.name = name;
        this.type = type;
    }
    public Argument() {}

    private String name;
    private String type;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
