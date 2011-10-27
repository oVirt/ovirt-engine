package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmAndAttachToUserParameters")
public class AddVmAndAttachToUserParameters extends AddVmFromScratchParameters {
    private static final long serialVersionUID = -5240624372284576720L;

    @XmlElement
    private boolean _fromScratch;

    @XmlElement(name = "UserId")
    private Guid _userId = new Guid();

    public AddVmAndAttachToUserParameters(VmStatic vmStatic, java.util.ArrayList<DiskImageBase> diskInfoList,
            boolean fromScratch, Guid userId, Guid storageDomainId) {
        super(vmStatic, diskInfoList, storageDomainId);
        _fromScratch = fromScratch;
        _userId = userId;
    }

    public boolean getFromScratch() {
        return _fromScratch;
    }

    public Guid getUserId() {
        return _userId;
    }

    public AddVmAndAttachToUserParameters() {
    }
}
