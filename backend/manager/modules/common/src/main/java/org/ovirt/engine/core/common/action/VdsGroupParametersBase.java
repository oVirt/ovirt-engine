package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsGroupParametersBase")
public class VdsGroupParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -9133528679053901135L;
    @XmlElement
    private Guid _vdsGroupId;

    public VdsGroupParametersBase(Guid vdsGroupId) {
        _vdsGroupId = vdsGroupId;
    }

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public VdsGroupParametersBase() {
    }
}
