package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ADElementTimeLeasedVmPoolParametersBase")
public class ADElementTimeLeasedVmPoolParametersBase extends VmPoolToAdElementParameters {
    private static final long serialVersionUID = -1930036899201922902L;
    @XmlElement
    private time_lease_vm_pool_map _map;

    public ADElementTimeLeasedVmPoolParametersBase(time_lease_vm_pool_map map) {
        super(map.getid(), map.getvm_pool_id());
        _map = map;
    }

    public time_lease_vm_pool_map getTimeLeasedVmPoolMap() {
        return _map;
    }

    public ADElementTimeLeasedVmPoolParametersBase() {
    }
}
