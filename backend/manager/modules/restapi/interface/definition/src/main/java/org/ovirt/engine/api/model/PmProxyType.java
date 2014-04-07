package org.ovirt.engine.api.model;

public enum PmProxyType {

    CLUSTER("cluster"), DC("dc"), OTHER_DC("other_dc");

    private String value;

    PmProxyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PmProxyType fromValue(String v) {
        try {
            if (v==null) {
                return null;
            }
            if (v.equals("cluster")) {
                return CLUSTER;
            } else if (v.equals("dc")) {
                return DC;
            } else if (v.equals("other_dc")) {
                return OTHER_DC;
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
