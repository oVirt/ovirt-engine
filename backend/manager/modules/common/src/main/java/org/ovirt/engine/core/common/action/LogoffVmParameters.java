package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LogoffVmParameters")
public class LogoffVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 937720357356448415L;
    @XmlElement
    private boolean _force;

    public LogoffVmParameters(Guid vmId, boolean force) {
        super(vmId);
        _force = force;
    }

    public boolean getForce() {
        return _force;
    }

    public LogoffVmParameters() {
    }
}
