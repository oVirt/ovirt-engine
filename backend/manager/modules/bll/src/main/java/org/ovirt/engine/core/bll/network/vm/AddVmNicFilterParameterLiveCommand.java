package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidateSupportsTransaction;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

@ValidateSupportsTransaction
public class AddVmNicFilterParameterLiveCommand<T extends VmNicFilterParameterParameters>
        extends AbstractVmNicFilterParameterCommand<T> {

    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Inject
    private VmNicFilterParameterValidator validator;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public AddVmNicFilterParameterLiveCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        VmNicFilterParameter filterParameter = getParameters().getFilterParameter();
        filterParameter.setId(Guid.newGuid());
        var vmNetworkInterface = vmNetworkInterfaceDao.get(getParameters().getFilterParameter().getVmInterfaceId());
        var addVmInterfaceParameters = new AddVmInterfaceParameters(getParameters().getVmId(), vmNetworkInterface);
        var allNetFilters = vmNicFilterParameterDao.getAllForVmNic(vmNetworkInterface.getId());
        allNetFilters.add(getParameters().getFilterParameter());
        addVmInterfaceParameters.setFilterParameters(allNetFilters);
        var returnValue = runInternalAction(ActionType.UpdateVmInterface, addVmInterfaceParameters, getContext());
        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
            return;
        }
        getReturnValue().setActionReturnValue(returnValue.getActionReturnValue());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        VmNicFilterParameter filterParameter = getParameters().getFilterParameter();

        return super.validate()
                && validate(validator.vmInterfaceHavingIdExists(filterParameter.getVmInterfaceId()))
                && validate(validator.vmInterfaceHavingIdExistsOnVmHavingId(
                        filterParameter.getVmInterfaceId(), getVmId()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmNicFilterParameterName", getParameters().getFilterParameter().getName());
        addCustomValue("VmNicFilterParameterId", getParameters().getFilterParameter().getId().toString());
        addCustomValue("VmInterfaceId", getParameters().getFilterParameter().getVmInterfaceId().toString());
        addCustomValue("VmName", getVm().getName());
        return getSucceeded() ? AuditLogType.NETWORK_ADD_NIC_FILTER_PARAMETER
                : AuditLogType.NETWORK_ADD_NIC_FILTER_PARAMETER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        super.setActionMessageParameters();
    }
}
