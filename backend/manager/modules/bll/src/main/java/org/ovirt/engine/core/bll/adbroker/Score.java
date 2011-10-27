package org.ovirt.engine.core.bll.adbroker;

public enum Score {
    LOW(0),
    HIGH(10);

    private Score(int val) {
        this.val = val;
    }

    final private int val;

    public int getValue() {
        return val;
    }

}
