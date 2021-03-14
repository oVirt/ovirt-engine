package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.ListUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

public class UpdateVmNicFilterParameterLiveCommand<T extends VmNicFilterParameterParameters> extends AbstractVmNicFilterParameterCommand<T> {

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private VmNicFilterParameterValidator validator;

    public UpdateVmNicFilterParameterLiveCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

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
    protected void executeVmCommand() {
        super.executeVmCommand();
        var vmNetworkInterface = vmNetworkInterfaceDao.get(getParameters().getFilterParameter().getVmInterfaceId());
        List<VmNicFilterParameter> unmodifiedNetFilters = vmNicFilterParameterDao
            .getAllForVmNic(vmNetworkInterface.getId())
            .stream()
            .filter(f -> !f.getId().equals(getParameters().getFilterParameter().getId()))
            .collect(Collectors.toList());
        var addVmInterfaceParameters = new AddVmInterfaceParameters(getParameters().getVmId(), vmNetworkInterface);
        addVmInterfaceParameters.setFilterParameters(
            ListUtils.union(unmodifiedNetFilters, Collections.singletonList(getParameters().getFilterParameter()))
        );
        var returnValue = runInternalAction(ActionType.UpdateVmInterface, addVmInterfaceParameters, getContext());
        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
            return;
        }
        getReturnValue().setActionReturnValue(returnValue.getActionReturnValue());
        setSucceeded(true);
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
