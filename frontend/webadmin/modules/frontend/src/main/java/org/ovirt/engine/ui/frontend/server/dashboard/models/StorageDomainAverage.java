package org.ovirt.engine.ui.frontend.server.dashboard.models;

public class StorageDomainAverage {
    private String name;
    private double value;

    public StorageDomainAverage(String name, double value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
