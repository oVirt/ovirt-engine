package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVGVDSCommandParameters")
@Deprecated
public class RemoveVGVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public RemoveVGVDSCommandParameters(Guid vdsId, String vgId) {
        super(vdsId);
        setVGID(vgId);
    }

    @XmlElement(name = "VGID")
    private String privateVGID;

    public String getVGID() {
        return privateVGID;
    }

    private void setVGID(String value) {
        privateVGID = value;
    }

    public RemoveVGVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, VGID=%s", super.toString(), getVGID());
    }
}
