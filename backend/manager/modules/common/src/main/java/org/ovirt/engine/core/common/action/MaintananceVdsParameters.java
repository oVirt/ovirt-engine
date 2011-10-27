package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MaintananceVdsParameters")
public class MaintananceVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = -962696566094119431L;
    @XmlElement
    private boolean _isInternal;

    public MaintananceVdsParameters(Guid vdsId, boolean isInternal) {
        super(vdsId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public MaintananceVdsParameters() {
    }
}
