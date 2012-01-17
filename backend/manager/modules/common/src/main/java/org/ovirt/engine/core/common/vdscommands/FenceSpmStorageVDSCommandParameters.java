package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FenceSpmStorageVDSCommandParameters")
public class FenceSpmStorageVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public FenceSpmStorageVDSCommandParameters(Guid vdsId, Guid storagePoolId, int prevID, String prevLVER) {
        super(vdsId, storagePoolId);
        setPrevId(prevID);
        setPrevLVER((prevLVER != null) ? prevLVER : "-1");
    }

    @XmlElement(name = "PrevId")
    private int privatePrevId;

    public int getPrevId() {
        return privatePrevId;
    }

    private void setPrevId(int value) {
        privatePrevId = value;
    }

    @XmlElement(name = "PrevLVER")
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
