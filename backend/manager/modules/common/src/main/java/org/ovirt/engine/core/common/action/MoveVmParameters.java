package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveVmParameters")
public class MoveVmParameters extends MoveOrCopyParameters implements java.io.Serializable {
    private static final long serialVersionUID = -168358966446399575L;

    public MoveVmParameters(Guid vmId, Guid storageDomainId) {
        super(vmId, storageDomainId);
    }

    public MoveVmParameters() {
    }
}
