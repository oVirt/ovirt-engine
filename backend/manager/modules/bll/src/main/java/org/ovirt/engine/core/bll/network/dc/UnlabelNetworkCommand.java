package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.UnlabelNetworkParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class UnlabelNetworkCommand<T extends UnlabelNetworkParameters> extends CommandBase<T> {

    @Inject
    private NetworkDao networkDao;

    private Network network;

    public UnlabelNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setStoragePoolId(getNetwork() == null ? null : getNetwork().getDataCenterId());
    }

    @Override
    protected void executeCommand() {
        getNetwork().setLabel(null);
        ActionReturnValue result = runInternalAction(ActionType.UpdateNetwork,
                new AddNetworkStoragePoolParameters(getNetwork().getDataCenterId(), getNetwork()));

        if (!result.getSucceeded()) {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__LABEL);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        NetworkValidator validatorNew = new NetworkValidator(getNetwork());
        return validate(validatorNew.networkIsSet(getParameters().getNetworkId()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UNLABEL_NETWORK : AuditLogType.UNLABEL_NETWORK_FAILED;
    }

    private Network getNetwork() {
        if (network == null) {
            network = networkDao.get(getParameters().getNetworkId());
        }

        return network;
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid networkId = getNetwork() == null ? null : getNetwork().getId();
        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
