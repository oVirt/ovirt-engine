package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmPoolParametersBase")
public class VmPoolParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -4244908570752388901L;
    @XmlElement
    private NGuid _vmPoolId;

    public VmPoolParametersBase(NGuid vmPoolId) {
        _vmPoolId = vmPoolId;
    }

    public NGuid getVmPoolId() {
        return _vmPoolId;
    }

    public void setVmPoolId(NGuid value) {
        _vmPoolId = value;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public VmPoolParametersBase() {
    }
}
