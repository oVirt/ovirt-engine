package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

public class RemoveVmNicFilterParameterCommand<T extends RemoveVmNicFilterParameterParameters>
        extends AbstractVmNicFilterParameterCommand<T>  {

    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Inject
    private VmNicFilterParameterValidator validator;

    public RemoveVmNicFilterParameterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();

        CompensationUtils.removeEntity(getParameters().getFilterParameterId(), vmNicFilterParameterDao,
                getCompensationContextIfEnabledByCaller());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        return super.validate()
                && validate(validator.parameterHavingIdExists(getParameters().getFilterParameterId()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmNicFilterParameterId", getParameters().getFilterParameterId().toString());
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NIC_FILTER_PARAMETER
                : AuditLogType.NETWORK_REMOVE_NIC_FILTER_PARAMETER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        super.setActionMessageParameters();
    }
}
