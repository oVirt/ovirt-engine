package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmToAdGroupParameters")
public class VmToAdGroupParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = -3998069506955069055L;
    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public VmToAdGroupParameters(Guid vmId, ad_groups adGroup) {
        super(adGroup);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VmToAdGroupParameters() {
    }
}
