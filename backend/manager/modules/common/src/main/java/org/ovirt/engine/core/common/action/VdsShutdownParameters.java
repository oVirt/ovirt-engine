package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsShutdownParameters")
public class VdsShutdownParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6589404824277164961L;
    @XmlElement(name = "Reboot")
    private boolean privateReboot;

    public boolean getReboot() {
        return privateReboot;
    }

    private void setReboot(boolean value) {
        privateReboot = value;
    }

    public VdsShutdownParameters(Guid vdsId, boolean reboot) {
        super(vdsId);
        setReboot(reboot);
    }

    public VdsShutdownParameters() {
    }
}
