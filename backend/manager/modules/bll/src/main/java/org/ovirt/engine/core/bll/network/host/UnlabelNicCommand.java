package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class UnlabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    @Inject
    private InterfaceDao interfaceDao;

    private VdsNetworkInterface nic;

    public UnlabelNicCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        addCustomValue("NicName", getNic().getName());

        ActionReturnValue result =
                runInternalAction(ActionType.HostSetupNetworks,
                        createHostSetupNetworksParameters(), cloneContextAndDetachFromParent());

        if (!result.getSucceeded()) {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    private HostSetupNetworksParameters createHostSetupNetworksParameters() {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(getVdsId());
        params.setRemovedLabels(Collections.singleton(getLabel()));
        return params;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__LABEL);
    }

    @Override
    protected boolean validate() {
        Guid nicId = getParameters().getNicId();
        HostInterfaceValidator hostInterfaceValidator = new HostInterfaceValidator(getNic());

        return validate(hostInterfaceValidator.interfaceExists(nicId))
                && validate(hostInterfaceValidator.nicIsNotLabeledWithSpecifiedLabel(getLabel()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UNLABEL_NIC : AuditLogType.UNLABEL_NIC_FAILED;
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = interfaceDao.get(getParameters().getNicId());
        }

        return nic;
    }

    public String getLabel() {
        return getParameters().getLabel();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid hostId = getNic() == null ? null : getNic().getVdsId();
        return Collections.singletonList(new PermissionSubject(hostId,
                VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }
}
