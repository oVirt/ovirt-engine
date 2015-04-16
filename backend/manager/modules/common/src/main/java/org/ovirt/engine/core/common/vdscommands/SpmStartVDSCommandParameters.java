package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.RecoveryMode;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SpmStartVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public SpmStartVDSCommandParameters(Guid vdsId, Guid storagePoolId, int prevID, String prevLVER,
            RecoveryMode recoveryMode, boolean SCSIfencing, StorageFormatType storagePoolFormatType) {
        super(vdsId, storagePoolId);
        setPrevId(prevID);
        setPrevLVER((prevLVER != null) ? prevLVER : "-1");
        setRecoveryMode(recoveryMode);
        setSCSIFencing(SCSIfencing);
        setStoragePoolFormatType(storagePoolFormatType);
    }

    private StorageFormatType storagePoolFormatType;

    public StorageFormatType getStoragePoolFormatType() {
        return storagePoolFormatType;
    }

    public void setStoragePoolFormatType(StorageFormatType storagePoolFormatType) {
        this.storagePoolFormatType = storagePoolFormatType;
    }

    private RecoveryMode privateRecoveryMode;

    public RecoveryMode getRecoveryMode() {
        return privateRecoveryMode;
    }

    private void setRecoveryMode(RecoveryMode value) {
        privateRecoveryMode = value;
    }

    private boolean privateSCSIFencing;

    public boolean getSCSIFencing() {
        return privateSCSIFencing;
    }

    private void setSCSIFencing(boolean value) {
        privateSCSIFencing = value;
    }

    public SpmStartVDSCommandParameters() {
        privateRecoveryMode = RecoveryMode.Manual;
    }

    private int privatePrevId;

    public int getPrevId() {
        return privatePrevId;
    }

    private void setPrevId(int value) {
        privatePrevId = value;
    }

    private String privatePrevLVER;

    public String getPrevLVER() {
        return privatePrevLVER;
    }

    private void setPrevLVER(String value) {
        privatePrevLVER = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("prevId", getPrevId())
                .append("prevLVER", getPrevLVER())
                .append("storagePoolFormatType", getStoragePoolFormatType())
                .append("recoveryMode", getRecoveryMode())
                .append("SCSIFencing", getSCSIFencing());
    }
}
