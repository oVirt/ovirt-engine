package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.users.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmPoolUserParameters")
public class VmPoolUserParameters extends VmPoolSimpleUserParameters implements java.io.Serializable {
    private static final long serialVersionUID = -5672324868972973061L;

    public VmPoolUserParameters(Guid vmPoolId, VdcUser user, boolean isInternal) {
        super(vmPoolId, user.getUserId());
        setVdcUserData(user);
        setIsInternal(isInternal);
    }

    @XmlElement(name = "VdcUserData")
    private VdcUser privateVdcUserData;

    public VdcUser getVdcUserData() {
        return privateVdcUserData;
    }

    private void setVdcUserData(VdcUser value) {
        privateVdcUserData = value;
    }

    @XmlElement(name = "IsInternal")
    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    @XmlElement(name = "VmId")
    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    public void setVmId(Guid value) {
        privateVmId = value;
    }

    public VmPoolUserParameters() {
    }
}
