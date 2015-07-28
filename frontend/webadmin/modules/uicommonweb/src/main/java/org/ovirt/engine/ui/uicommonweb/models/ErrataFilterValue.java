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

    protected boolean isSecurity() {
        return security;
    }

    protected boolean isBugs() {
        return bugs;
    }

    protected boolean isEnhancements() {
        return enhancements;
    }

}
