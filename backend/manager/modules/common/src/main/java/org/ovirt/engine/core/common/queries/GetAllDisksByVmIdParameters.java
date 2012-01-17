package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllDisksByVmIdParameters")
public class GetAllDisksByVmIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1277592901283660498L;

    public GetAllDisksByVmIdParameters(Guid vmId) {
        _vmId = vmId;
    }

    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public Guid getVmId() {
        return _vmId;
    }

    public GetAllDisksByVmIdParameters() {
    }
}
