package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsActionParameters")
public class VdsActionParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 3959465593772384532L;

    public VdsActionParameters(Guid vdsId) {
        _vdsId = vdsId;
    }

    @XmlElement(name = "VdsId")
    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public void setVdsId(Guid value) {
        _vdsId = value;
    }

    public VdsActionParameters() {
    }
}
