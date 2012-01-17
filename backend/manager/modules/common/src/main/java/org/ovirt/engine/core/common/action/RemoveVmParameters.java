package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVmParameters")
public class RemoveVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -6256468461166321723L;
    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    private void setForce(boolean value) {
        privateForce = value;
    }

    public RemoveVmParameters(Guid vmId, boolean force) {
        super(vmId);
        setForce(force);
    }

    public RemoveVmParameters() {
    }
}
