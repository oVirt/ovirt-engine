package org.ovirt.engine.ui.frontend.server.dashboard;

/**
 * This class represents on block in the heat map for a particular resource.
 */
public class HeatMapBlock {
    private double value;
    private String name;

    public HeatMapBlock(String name, double value) {
        setValue(value);
        setName(name);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String toolTip) {
        this.name = toolTip;
    }
}
