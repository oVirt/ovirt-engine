package org.ovirt.engine.ui.frontend.server.dashboard;

public class HistoryNode {

    private long date;
    private double value;

    public HistoryNode(long date, double value) {
        setDate(date);
        setValue(value);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
