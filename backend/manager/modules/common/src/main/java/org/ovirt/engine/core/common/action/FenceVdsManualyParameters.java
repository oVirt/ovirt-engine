package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FenceVdsManualyParameters")
public class FenceVdsManualyParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -5313772300704786422L;
    @XmlElement(name = "ClearVMs")
    private boolean privateClearVMs;

    public boolean getClearVMs() {
        return privateClearVMs;
    }

    public void setClearVMs(boolean value) {
        privateClearVMs = value;
    }

    public FenceVdsManualyParameters(boolean clearVMs) {
        super(Guid.Empty);
        setClearVMs(clearVMs);
    }

    public FenceVdsManualyParameters() {
    }
}
