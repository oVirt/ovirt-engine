package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.users.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmToUserParameters")
public class VmToUserParameters extends VmToAdElementParameters {
    private static final long serialVersionUID = -4014070637785795975L;
    @XmlElement(name = "User")
    private VdcUser _user;

    public VmToUserParameters(VdcUser user, Guid vmId) {
        super(user.getUserId(), vmId);
        _user = user;
    }

    public VdcUser getUser() {
        return _user;
    }

    public VmToUserParameters() {
    }
}
