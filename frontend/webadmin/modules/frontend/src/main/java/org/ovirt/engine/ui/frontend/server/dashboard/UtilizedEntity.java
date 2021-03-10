package org.ovirt.engine.ui.frontend.server.dashboard;

import com.fasterxml.jackson.annotation.JsonValue;

public class UtilizedEntity {

    public enum Trend {

        UP,
        DOWN,
        SAME;

        @JsonValue
        public String toValue() {
            return name().toLowerCase();
        }

    }

    private String name;
    private Double used;
    private Double total;
    private Trend trend;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double value) {
        this.total = value;
    }

    public Trend getTrend() {
        return trend;
    }

    public void setTrend(Trend trend) {
        this.trend = trend;
    }

    public Double getUsed() {
        return used;
    }

    public void setUsed(Double used) {
        this.used = used;
    }
}
