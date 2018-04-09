package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class SpmStatusResult implements Serializable {
    private static final long serialVersionUID = 7385117784725872207L;
    private SpmStatus spmStatus;

    public SpmStatus getSpmStatus() {
        return spmStatus;
    }

    public void setSpmStatus(SpmStatus value) {
        spmStatus = value;
    }

    private String spmLVER;

    public String getSpmLVER() {
        return spmLVER;
    }

    public void setSpmLVER(String value) {
        spmLVER = value;
    }

    private int spmId;

    public int getSpmId() {
        return spmId;
    }

    public void setSpmId(int value) {
        spmId = value;
    }

    public SpmStatusResult() {
        spmStatus = SpmStatus.SPM;
    }
}
