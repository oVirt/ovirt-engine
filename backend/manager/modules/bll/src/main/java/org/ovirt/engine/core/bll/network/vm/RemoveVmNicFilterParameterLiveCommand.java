package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

public class RemoveVmNicFilterParameterLiveCommand<T extends RemoveVmNicFilterParameterParameters>
        extends AbstractVmNicFilterParameterCommand<T>  {

    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Inject
    private VmNicFilterParameterValidator validator;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public RemoveVmNicFilterParameterLiveCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        var vmNicFilterParameter = vmNicFilterParameterDao.get(getParameters().getFilterParameterId());
        var vmNetworkInterface = vmNetworkInterfaceDao.get(vmNicFilterParameter.getVmInterfaceId());
        var addVmInterfaceParameters = new AddVmInterfaceParameters(getParameters().getVmId(), vmNetworkInterface);
        var allNetFilters = vmNicFilterParameterDao.getAllForVmNic(vmNetworkInterface.getId());
        allNetFilters.remove(vmNicFilterParameter);
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
