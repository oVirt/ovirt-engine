package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmOperationParameterBase")
public class VmOperationParameterBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -6248335374537898949L;
    private Guid quotaId;

    public VmOperationParameterBase() {
    }

    private Guid _vmId = Guid.Empty;

    public VmOperationParameterBase(Guid vmId) {
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public void setVmId(Guid value) {
        _vmId = value;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid value) {
        quotaId = value;
    }
}
