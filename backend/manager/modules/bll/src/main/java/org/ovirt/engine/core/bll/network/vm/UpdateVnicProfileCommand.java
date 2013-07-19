package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

public class UpdateVnicProfileCommand<T extends VnicProfileParameters> extends VnicProfileCommandBase<T> {

    public UpdateVnicProfileCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        VnicProfileValidator validator = new VnicProfileValidator(getVnicProfile());
        return validate(validator.vnicProfileIsSet())
                && validate(validator.vnicProfileExists())
                && validate(validator.vnicProfileNameNotUsed())
                && validate(validator.networkNotChanged())
                && validate(validator.portMirroringNotChangedIfUsedByVms());
    }

    @Override
    protected void executeCommand() {
        getVnicProfileDao().update(getVnicProfile());
        setSucceeded(true);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UPDATE_VNIC_PROFILE
                : AuditLogType.UPDATE_VNIC_PROFILE_FAILED;
    }
}
