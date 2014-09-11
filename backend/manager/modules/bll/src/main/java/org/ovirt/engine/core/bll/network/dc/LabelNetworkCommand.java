package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.LabelNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class LabelNetworkCommand<T extends LabelNetworkParameters> extends CommandBase<T> {
    private Network network;

    public LabelNetworkCommand(T parameters) {
        super(parameters);
        setStoragePoolId(getNetwork() == null ? null : getNetwork().getDataCenterId());
    }

    @Override
    protected void executeCommand() {
        getNetwork().setLabel(getLabel());
        VdcReturnValueBase result = runInternalAction(VdcActionType.UpdateNetwork,
                new AddNetworkStoragePoolParameters(getNetwork().getDataCenterId(), getNetwork()));

        if (!result.getSucceeded()) {
            propagateFailure(result);
        }

        getReturnValue().setActionReturnValue(getLabel());
        setSucceeded(result.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__LABEL);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    protected boolean canDoAction() {
        NetworkValidator validator = new NetworkValidator(getNetwork());
        return validate(validator.networkIsSet())
                && validate(validator.notLabeled())
                && validate(validator.notExternalNetwork());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.LABEL_NETWORK : AuditLogType.LABEL_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    private Network getNetwork() {
        if (network == null) {
            network = getNetworkDAO().get(getParameters().getNetworkId());
        }

        return network;
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }

    public String getLabel() {
        return getParameters().getLabel();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid networkId = getNetwork() == null ? null : getNetwork().getId();
        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
