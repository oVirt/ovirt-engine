package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AdElementParametersBase")
public class AdElementParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;
    @XmlElement
    private Guid _adElementId = new Guid();

    public AdElementParametersBase(Guid adElementId) {
        _adElementId = adElementId;
    }

    public Guid getAdElementId() {
        return _adElementId;
    }

    public AdElementParametersBase() {
    }
}
