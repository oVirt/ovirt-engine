package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

public class FopStats implements Serializable {

    private static final long serialVersionUID = 3705558577202227092L;
    private String name;
    private int hits;
    private double avgLatency;
    private double minLatency;
    private double maxLatency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public double getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(double avgLatency) {
        this.avgLatency = avgLatency;
    }

    public double getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(double minLatency) {
        this.minLatency = minLatency;
    }

    public double getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(double maxLatency) {
        this.maxLatency = maxLatency;
    }
}
