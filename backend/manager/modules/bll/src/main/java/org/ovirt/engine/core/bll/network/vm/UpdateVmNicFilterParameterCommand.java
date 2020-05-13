package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

public class UpdateVmNicFilterParameterCommand<T extends VmNicFilterParameterParameters>
        extends AbstractVmNicFilterParameterCommand<T> {

    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Inject
    private VmNicFilterParameterValidator validator;

    public UpdateVmNicFilterParameterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();

        CompensationUtils.updateEntity(getParameters().getFilterParameter(), vmNicFilterParameterDao,
                getCompensationContextIfEnabledByCaller());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        VmNicFilterParameter filterParameter = getParameters().getFilterParameter();

        return super.validate()
                && validate(validator.parameterHavingIdExists(filterParameter.getId()))
                && validate(validator.vmInterfaceHavingIdExists(filterParameter.getVmInterfaceId()))
                && validate(validator.vmInterfaceHavingIdExistsOnVmHavingId(
                        filterParameter.getVmInterfaceId(), getVmId()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmNicFilterParameterName", getParameters().getFilterParameter().getName());
        addCustomValue("VmNicFilterParameterId", getParameters().getFilterParameter().getId().toString());
        addCustomValue("VmInterfaceId", getParameters().getFilterParameter().getVmInterfaceId().toString());
        addCustomValue("VmName", getVm().getName());
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_NIC_FILTER_PARAMETER
                : AuditLogType.NETWORK_UPDATE_NIC_FILTER_PARAMETER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        super.setActionMessageParameters();
    }
}
