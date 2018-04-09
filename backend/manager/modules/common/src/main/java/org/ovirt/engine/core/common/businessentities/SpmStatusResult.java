package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

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

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("SPM Id", spmId)
                .append("SPM LVER", spmLVER)
                .append("SPM Status", spmStatus)
                .build();
    }
}
