package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MigrateVmToServerParameters")
public class MigrateVmToServerParameters extends MigrateVmParameters {
    private static final long serialVersionUID = 2378358850714143232L;
    @XmlElement(name = "VdsId")
    private Guid vdsId;

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId) {
        super(forceMigration, vmId);
        vdsId = serverId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public MigrateVmToServerParameters() {
    }
}
