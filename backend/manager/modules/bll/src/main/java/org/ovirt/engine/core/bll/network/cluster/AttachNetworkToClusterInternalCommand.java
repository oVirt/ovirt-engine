package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.CanDoActionSupportsTransaction;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirectorDelegator;

@InternalCommandAttribute
@CanDoActionSupportsTransaction
public class AttachNetworkToClusterInternalCommand<T extends AttachNetworkToVdsGroupParameter> extends
                                                                                       NetworkClusterCommandBase<T> {

    AttachNetworkToClusterInternalCommand(T parameters) {
        super(parameters);
    }

    public AttachNetworkToClusterInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    @Override
    protected void executeCommand() {

        attachNetwork(getVdsGroupId(), getNetworkCluster(), getNetwork());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return networkNotAttachedToCluster()
                && vdsGroupExists()
                && changesAreClusterCompatible()
                && logicalNetworkExists()
                && validateAttachment();
    }

    private boolean validateAttachment() {
        final AttachNetworkClusterValidator attachNetworkClusterValidator = createNetworkClusterValidator();
        return validate(attachNetworkClusterValidator.networkBelongsToClusterDataCenter(getVdsGroup(),
                getPersistedNetwork())) &&
               validateAttachment(attachNetworkClusterValidator);
    }

    private AttachNetworkClusterValidator createNetworkClusterValidator() {
        return new AttachNetworkClusterValidator(getNetworkCluster(), getClusterVersion());
    }

    private Version getClusterVersion() {
        return getVdsGroup().getCompatibilityVersion();
    }

    private boolean logicalNetworkExists() {
        if (getPersistedNetwork() != null) {
            return true;
        }

        addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
        return false;
    }

    private boolean changesAreClusterCompatible() {
        if (!getParameters().getNetwork().isVmNetwork()) {
            if (!FeatureSupported.nonVmNetwork(getVdsGroup().getCompatibilityVersion())) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
                return false;
            }
        }
        return true;
    }

    private boolean networkNotAttachedToCluster() {
        if (networkExists()) {
            return failCanDoAction(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_CLUSTER);
        }

        return true;
    }

    private boolean networkExists() {
        return getNetworkClusterDAO().get(getNetworkCluster().getId()) != null;
    }

    private boolean vdsGroupExists() {
        if (!vdsGroupInDb()) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        return true;
    }

    private boolean vdsGroupInDb() {
        return getVdsGroup() != null;
    }

    private void attachNetwork(Guid clusterId, NetworkCluster networkCluster, Network network) {
        getNetworkClusterDAO().save(new NetworkCluster(clusterId, network.getId(),
                NetworkStatus.OPERATIONAL,
                false,
                networkCluster.isRequired(),
                false,
                false));

        if (network.getCluster().isDisplay()) {
            final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                    getNetworkClusterDAO(),
                    getVmDAO(),
                    networkCluster,
                    network.getName(),
                    AuditLogDirectorDelegator.getInstance());
            if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
                displayNetworkClusterHelper.warnOnActiveVm();
            }

            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(clusterId, network.getId());
        }

        if (network.getCluster().isMigration()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsMigration(clusterId, network.getId());
        }

        NetworkClusterHelper.setStatus(clusterId, network);
    }

    @Override
    protected String getDescription() {
        String networkName = getNetworkName() == null ? "" : getNetworkName();
        String clusterName = getVdsGroup() == null ? "" : getVdsGroup().getName();
        return networkName + " - " + clusterName;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP
                             : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED;
    }
}
