package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VDSDomainsData implements Serializable {
    private static final long serialVersionUID = 8446715112443992758L;
    private Guid privateDomainId;
    private double lastCheck;
    private double delay;
    private boolean actual;
    private boolean acquired;

    public Guid getDomainId() {
        return privateDomainId;
    }

    public void setDomainId(Guid value) {
        privateDomainId = value;
    }

    private int privateCode;

    public int getCode() {
        return privateCode;
    }

    public void setCode(int value) {
        privateCode = value;
    }

    public double getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(double lastCheck) {
        this.lastCheck = lastCheck;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public double getDelay() {
        return delay;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
    }

    public VDSDomainsData() {
        privateDomainId = Guid.Empty;
    }

    public boolean isAcquired() {
        return acquired;
    }

    public void setAcquired(boolean acquired) {
        this.acquired = acquired;
    }
}
