package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateUserVmPoolParameters")
public class UpdateUserVmPoolParameters extends VmPoolSimpleUserParameters {
    private static final long serialVersionUID = -95547459697168818L;
    @XmlElement
    private time_lease_vm_pool_map _userPoolMap;

    public UpdateUserVmPoolParameters(time_lease_vm_pool_map map) {
        super(map.getvm_pool_id(), map.getid());
        _userPoolMap = map;
    }

    public time_lease_vm_pool_map getUserPoolMap() {
        return _userPoolMap;
    }

    public UpdateUserVmPoolParameters() {
    }
}
