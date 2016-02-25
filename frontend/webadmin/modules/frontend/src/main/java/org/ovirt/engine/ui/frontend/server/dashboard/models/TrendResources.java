package org.ovirt.engine.ui.frontend.server.dashboard.models;

public class TrendResources {
    private String name;
    private double total;
    private double used;
    private double previousUsed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    public double getPreviousUsed() {
        return previousUsed;
    }

    public void setPreviousUsed(double previousUsed) {
        this.previousUsed = previousUsed;
    }

}
