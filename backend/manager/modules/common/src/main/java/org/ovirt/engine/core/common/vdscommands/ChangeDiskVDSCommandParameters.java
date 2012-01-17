package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ChangeDiskVDSCommandParameters")
public class ChangeDiskVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    @XmlElement
    private String _diskPath;

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String diskPath) {
        super(vdsId, vmId);
        _diskPath = diskPath;
    }

    public String getDiskPath() {
        return _diskPath;
    }

    public ChangeDiskVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, diskPath=%s", super.toString(), getDiskPath());
    }
}
