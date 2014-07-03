package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ForemanOperatingSystem implements Serializable {

    private static final long serialVersionUID = 5278645435607997741L;

    int id;
    String name;
    String major;
    String minor;
    String family;

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
