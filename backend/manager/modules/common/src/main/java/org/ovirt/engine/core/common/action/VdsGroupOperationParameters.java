package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class VdsGroupOperationParameters extends VdsGroupParametersBase {
    private static final long serialVersionUID = -2184123302248929010L;
    @Valid
    private VDSGroup _vdsGroup;

    public VdsGroupOperationParameters(VDSGroup group) {
        super(group.getId());
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
