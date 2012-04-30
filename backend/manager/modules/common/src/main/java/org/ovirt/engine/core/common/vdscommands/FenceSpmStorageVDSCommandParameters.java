package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class FenceSpmStorageVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public FenceSpmStorageVDSCommandParameters(Guid vdsId, Guid storagePoolId, int prevID, String prevLVER) {
        super(vdsId, storagePoolId);
        setPrevId(prevID);
        setPrevLVER((prevLVER != null) ? prevLVER : "-1");
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

    public FenceSpmStorageVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, prevId=%s, prevLVER=%s", super.toString(), getPrevId(), getPrevLVER());
    }
}
