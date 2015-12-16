package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.dao.VmDao;

public class RemoveVnicProfileCommand<T extends VnicProfileParameters> extends VnicProfileCommandBase<T> {

    @Inject
    private VmDao vmDao;

    public RemoveVnicProfileCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean validate() {
        VnicProfileValidator validator = new VnicProfileValidator(vmDao, getVnicProfile());
        return validate(validator.vnicProfileIsSet())
                && validate(validator.vnicProfileExists())
                && validate(validator.vnicProfileNotUsedByVms())
                && validate(validator.vnicProfileNotUsedByTemplates());
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
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.REMOVE_VNIC_PROFILE
                : AuditLogType.REMOVE_VNIC_PROFILE_FAILED;
    }
}
