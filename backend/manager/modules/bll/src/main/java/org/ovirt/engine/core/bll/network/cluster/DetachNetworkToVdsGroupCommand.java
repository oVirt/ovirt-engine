package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class DetachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends VdsGroupCommandBase<T> {

    private Network persistedNetwork;

    @Inject
    private DetachNetworkClusterPermissionFinder permissionFinder;

    public DetachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        final VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.DetachNetworkFromClusterInternal, getParameters());

        setSucceeded(returnValue.getSucceeded());

        if (returnValue.getSucceeded()) {
            if (NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getPersistedNetwork(), getVdsGroup().getCompatibilityVersion())) {
                detachLabeledNetworksFromClusterHosts();
            }
        } else {
            propagateFailure(returnValue);
        }
    }

    private void detachLabeledNetworksFromClusterHosts() {
        final AttachNetworkToVdsGroupParameter attachNetworkToVdsGroupParameter = getParameters();

        runInternalAction(
                VdcActionType.PropagateLabeledNetworksToClusterHosts,
                new ManageNetworkClustersParameters(
                        Collections.<NetworkCluster>emptyList(),
                        new ArrayList<>(Collections.singleton(attachNetworkToVdsGroupParameter.getNetworkCluster()))));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP
                : AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__DETACH);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return permissionFinder.findPermissionCheckSubjects(getNetworkId(), getActionType());
    }

    private Guid getNetworkId() {
        return getNetwork() == null ? null : getNetwork().getId();
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    private Network getPersistedNetwork() {
        if (persistedNetwork == null) {
            persistedNetwork = getNetworkDao().get(getNetwork().getId());
        }

        return persistedNetwork;
    }
}
