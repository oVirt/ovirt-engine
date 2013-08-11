package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.RecoveryMode;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class SpmStartVDSCommandParameters extends FenceSpmStorageVDSCommandParameters {
    public SpmStartVDSCommandParameters(Guid vdsId, Guid storagePoolId, int prevID, String prevLVER,
            RecoveryMode recoveryMode, boolean SCSIfencing, StorageFormatType storagePoolFormatType) {
        super(vdsId, storagePoolId, prevID, prevLVER);
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

    @Override
    public String toString() {
        return String.format("%s, storagePoolFormatType=%s, recoveryMode=%s, SCSIFencing=%s",
                super.toString(),
                getStoragePoolFormatType(),
                getRecoveryMode(),
                getSCSIFencing());
    }
}
