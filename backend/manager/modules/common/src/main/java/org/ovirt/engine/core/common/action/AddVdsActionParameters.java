package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVdsActionParameters")
public class AddVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = 8452910234577071082L;

    public AddVdsActionParameters(VdsStatic vdsStatic, String rootPassword) {
        super(vdsStatic, rootPassword);
    }

    private Guid vdsId;

    @XmlElement(name = "AddPending")
    private boolean privateAddPending;

    public boolean getAddPending() {
        return privateAddPending;
    }

    public void setAddPending(boolean value) {
        privateAddPending = value;
    }

    public AddVdsActionParameters() {
    }

    public void setVdsForUniqueId(Guid serverForUniqueId) {
        this.vdsId = serverForUniqueId;
    }

    public Guid getVdsForUniqueId() {
        return vdsId;
    }
}
