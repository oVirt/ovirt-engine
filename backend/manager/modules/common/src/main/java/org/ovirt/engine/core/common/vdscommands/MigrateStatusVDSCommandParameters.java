package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MigrateStatusVDSCommandParameters")
public class MigrateStatusVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
     public MigrateStatusVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public MigrateStatusVDSCommandParameters() {
    }
}
