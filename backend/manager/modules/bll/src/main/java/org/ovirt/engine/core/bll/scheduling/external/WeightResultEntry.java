package org.ovirt.engine.core.bll.scheduling.external;

import org.ovirt.engine.core.compat.Guid;

public class WeightResultEntry {
    private Guid host;
    private String weightUnit;
    private Integer weight;

    public WeightResultEntry(Guid host, Integer weight, String weightUnit) {
        this.host = host;
        this.weight = weight;
        this.weightUnit = weightUnit;
    }

    public Guid getHost() {
        return host;
    }

    public Integer getWeight() {
        return weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }
}
