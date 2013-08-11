package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class SpmStatusResult implements Serializable {
    private static final long serialVersionUID = -2043744550859733845L;
    private SpmStatus privateSpmStatus;

    public SpmStatus getSpmStatus() {
        return privateSpmStatus;
    }

    public void setSpmStatus(SpmStatus value) {
        privateSpmStatus = value;
    }

    private String privateSpmLVER;

    public String getSpmLVER() {
        return privateSpmLVER;
    }

    public void setSpmLVER(String value) {
        privateSpmLVER = value;
    }

    private int privateSpmId;

    public int getSpmId() {
        return privateSpmId;
    }

    public void setSpmId(int value) {
        privateSpmId = value;
    }

    public SpmStatusResult() {
        privateSpmStatus = SpmStatus.SPM;
    }
}
