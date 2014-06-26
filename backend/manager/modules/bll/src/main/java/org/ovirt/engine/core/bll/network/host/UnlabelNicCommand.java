package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.network.RemoveNetworksByLabelParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class UnlabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    private VdsNetworkInterface nic;

    public UnlabelNicCommand(T parameters) {
        super(parameters);
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                runInternalAction(VdcActionType.PersistentSetupNetworks,
                        new RemoveNetworksByLabelParametersBuilder(getContext()).buildParameters(getNic(),
                                getLabel(),
                                getVds().getVdsGroupId()), cloneContextAndDetachFromParent());

        if (!result.getSucceeded()) {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__LABEL);
    }

    @Override
    protected boolean canDoAction() {
        if (getNic() == null) {
            return failCanDoAction(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST);
        }

        if (!NetworkUtils.isLabeled(getNic()) || !getNic().getLabels().contains(getLabel())) {
            return failCanDoAction(VdcBllMessages.INTERFACE_NOT_LABELED);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UNLABEL_NIC : AuditLogType.UNLABEL_NIC_FAILED;
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = getDbFacade().getInterfaceDao().get(getParameters().getNicId());
        }

        return nic;
    }

    public String getNickName() {
        return getNic().getName();
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
