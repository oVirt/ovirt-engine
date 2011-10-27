package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachAdGroupTimeLeasedPoolCommandParameters")
public class AttachAdGroupTimeLeasedPoolCommandParameters extends ADElementTimeLeasedVmPoolParametersBase {
    private static final long serialVersionUID = 484523812183776047L;
    @XmlElement
    private ad_groups _adGroup;

    public AttachAdGroupTimeLeasedPoolCommandParameters(ad_groups group, time_lease_vm_pool_map map) {
        super(map);
        _adGroup = group;
    }

    public ad_groups getAdGroup() {
        return _adGroup;
    }

    public AttachAdGroupTimeLeasedPoolCommandParameters() {
    }
}
