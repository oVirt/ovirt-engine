package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MonitorCommandParameters")
public class MonitorCommandParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 3727810581213783933L;
    @XmlElement
    private String _command;

    public MonitorCommandParameters(Guid vmId, String command) {
        super(vmId);
        _command = command;
    }

    public String getCommand() {
        return _command;
    }

    public MonitorCommandParameters() {
    }
}
