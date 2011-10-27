package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmPoolToAdGroupParameters")
public class VmPoolToAdGroupParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = 5695955304480728659L;

    public VmPoolToAdGroupParameters(Guid vmPoolId, ad_groups group, boolean isInternal) {
        super(group);
        setVmPoolId(vmPoolId);
        setIsInternal(isInternal);
    }

    @XmlElement(name = "VmPoolId")
    private Guid privateVmPoolId;

    public Guid getVmPoolId() {
        return privateVmPoolId;
    }

    private void setVmPoolId(Guid value) {
        privateVmPoolId = value;
    }

    @XmlElement(name = "IsInternal")
    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public VmPoolToAdGroupParameters() {
    }
}
