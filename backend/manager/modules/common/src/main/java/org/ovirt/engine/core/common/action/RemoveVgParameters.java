package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVgParameters")
public class RemoveVgParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = 508039533347909988L;
    @XmlElement(name = "VgId")
    private String privateVgId;

    public String getVgId() {
        return privateVgId;
    }

    public void setVgId(String value) {
        privateVgId = value;
    }

    public RemoveVgParameters(String vgId) {
        super(Guid.Empty);
        setVgId(vgId);
    }

    public RemoveVgParameters() {
    }
}
