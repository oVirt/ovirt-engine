package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsGroupOperationParameters")
public class VdsGroupOperationParameters extends VdsGroupParametersBase {
    private static final long serialVersionUID = -2184123302248929010L;
    @Valid
    @XmlElement
    private VDSGroup _vdsGroup;

    public VdsGroupOperationParameters(VDSGroup group) {
        super(group.getID());
        _vdsGroup = group;
    }

    public VDSGroup getVdsGroup() {
        return _vdsGroup;
    }

    private boolean privateIsInternalCommand;

    public boolean getIsInternalCommand() {
        return privateIsInternalCommand;
    }

    public void setIsInternalCommand(boolean value) {
        privateIsInternalCommand = value;
    }

    public VdsGroupOperationParameters() {
    }
}
