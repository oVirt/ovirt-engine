package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;

public class RemoveVnicProfileCommand<T extends VnicProfileParameters> extends VnicProfileCommandBase<T> {

    public RemoveVnicProfileCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getVnicProfileDao().remove(getVnicProfile().getId());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.REMOVE_VNIC_PROFILE
                : AuditLogType.REMOVE_VNIC_PROFILE_FAILED;
    }
}
