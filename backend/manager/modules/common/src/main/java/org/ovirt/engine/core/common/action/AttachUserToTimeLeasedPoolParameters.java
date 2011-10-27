package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.users.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachUserToTimeLeasedPoolParameters")
public class AttachUserToTimeLeasedPoolParameters extends VmPoolUserParameters {
    private static final long serialVersionUID = -2625973275633657557L;
    @XmlElement
    private time_lease_vm_pool_map _userPoolMap;

    public AttachUserToTimeLeasedPoolParameters(Guid vmPoolId, VdcUser user, time_lease_vm_pool_map map) {
        super(vmPoolId, user, false);
        _userPoolMap = map;
    }

    public time_lease_vm_pool_map getUserPoolMap() {
        return _userPoolMap;
    }

    public AttachUserToTimeLeasedPoolParameters() {
    }
}
