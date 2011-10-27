package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveBondParameters")
public class RemoveBondParameters extends VdsActionParameters {
    private static final long serialVersionUID = 8082833148763122313L;
    @XmlElement(name = "BondName")
    private String privateBondName;

    public String getBondName() {
        return privateBondName;
    }

    private void setBondName(String value) {
        privateBondName = value;
    }

    public RemoveBondParameters(Guid vdsId, String bondName) {
        super(vdsId);
        setBondName(bondName);
    }

    public RemoveBondParameters() {
    }
}
