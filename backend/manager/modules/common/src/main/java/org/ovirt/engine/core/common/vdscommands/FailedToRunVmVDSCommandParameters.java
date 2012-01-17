package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FailedToRunVmVDSCommandParameters")
public class FailedToRunVmVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public FailedToRunVmVDSCommandParameters(Guid vdsId) {
        super(vdsId);
    }

    public FailedToRunVmVDSCommandParameters() {
    }
}
