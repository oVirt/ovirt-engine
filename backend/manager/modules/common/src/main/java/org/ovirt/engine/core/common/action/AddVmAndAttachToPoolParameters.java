package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmAndAttachToPoolParameters")
public class AddVmAndAttachToPoolParameters extends AddVmFromScratchParameters implements java.io.Serializable {
    private static final long serialVersionUID = -2676528333942591702L;

    @XmlElement
    private Guid _poolId;

    @XmlElement
    private String _currentVmName;

    public AddVmAndAttachToPoolParameters(VmStatic currVm, Guid poolId, String currentVmName, Guid storageDomainId) {
        super(currVm, new java.util.ArrayList<DiskImageBase>(
                java.util.Arrays.asList(new DiskImageBase[] { new DiskImageBase() })), storageDomainId);
        _poolId = poolId;
        _currentVmName = currentVmName;
    }

    public Guid getPoolId() {
        return _poolId;
    }

    public String getCurrentVmName() {
        return _currentVmName;
    }

    public AddVmAndAttachToPoolParameters() {
    }
}
