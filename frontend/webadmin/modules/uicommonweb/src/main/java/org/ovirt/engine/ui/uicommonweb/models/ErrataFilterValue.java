package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Holds the status of values for which to filter a List of errata (singular: Erratum).
 */
public class ErrataFilterValue {

    protected boolean security;
    protected boolean bugs;
    protected boolean enhancements;

    public ErrataFilterValue(boolean security, boolean bugs, boolean enhancements) {
        this.security = security;
        this.bugs = bugs;
        this.enhancements = enhancements;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public void setBugs(boolean bugs) {
        this.bugs = bugs;
    }

    public void setEnhancements(boolean enhancements) {
        this.enhancements = enhancements;
    }

    public boolean isSecurity() {
        return security;
    }

    public boolean isBugs() {
        return bugs;
    }

    public boolean isEnhancements() {
        return enhancements;
    }

}
