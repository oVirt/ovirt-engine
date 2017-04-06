package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VnicProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

public class RemoveVnicProfileCommand<T extends VnicProfileParameters> extends VnicProfileCommandBase<T> {

    @Inject
    private VnicProfileDao vnicProfileDao;

    public RemoveVnicProfileCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VnicProfileValidator validator = createVnicProfileValidator();
        return validate(validator.vnicProfileIsSet())
                && validate(validator.vnicProfileExists())
                && validate(validator.vnicProfileNotUsedByVms())
                && validate(validator.vnicProfileNotUsedByTemplates());
    }

    @Override
    protected void executeCommand() {
        vnicProfileDao.remove(getVnicProfile().getId());
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
