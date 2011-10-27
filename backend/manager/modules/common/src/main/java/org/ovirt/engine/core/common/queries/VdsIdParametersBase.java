package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsIdParametersBase")
public class VdsIdParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -232268659060166995L;

    public VdsIdParametersBase(Guid vdsId) {
        _vdsId = vdsId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsId")
    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public VdsIdParametersBase() {
    }
}
