package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.Pair;

public class FopStats implements Serializable {

    private static final long serialVersionUID = 3705558577202227092L;
    private String name;
    private int hits;
    private double avgLatency;
    private Pair<Double, String> avgLatencyFormatted;
    private double minLatency;
    private Pair<Double, String> minLatencyFormatted;
    private double maxLatency;
    private Pair<Double, String> maxLatencyFormatted;

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

    public Pair<Double, String> getAvgLatencyFormatted() {
        return avgLatencyFormatted;
    }

    public void setAvgLatencyFormatted(Pair<Double, String> avgLatency) {
        this.avgLatencyFormatted = avgLatency;
    }

    public Pair<Double, String> getMinLatencyFormatted() {
        return minLatencyFormatted;
    }

    public void setMinLatencyFormatted(Pair<Double, String> minLatency) {
        this.minLatencyFormatted = minLatency;
    }

    public Pair<Double, String> getMaxLatencyFormatted() {
        return maxLatencyFormatted;
    }

    public void setMaxLatencyFormatted(Pair<Double, String> maxLatency) {
        this.maxLatencyFormatted = maxLatency;
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
