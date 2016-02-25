package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.List;

public class Status {
    private String type;
    private int count;
    private List<String> statusValues;

    public Status(String type, int count, List<String> statusValues) {
        this.type = type;
        this.count = count;
        this.statusValues = statusValues;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setStatusValues(List<String> values) {
        this.statusValues = values;
    }

    public List<String> getStatusValues() {
        return statusValues;
    }
}
