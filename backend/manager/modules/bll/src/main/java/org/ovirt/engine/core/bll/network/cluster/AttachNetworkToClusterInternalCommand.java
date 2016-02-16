package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CanDoActionSupportsTransaction;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkAttachmentsPersister;

@InternalCommandAttribute
@CanDoActionSupportsTransaction
public class AttachNetworkToClusterInternalCommand<T extends AttachNetworkToVdsGroupParameter>
    extends NetworkClusterCommandBase<T> {

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private InterfaceDao interfaceDao;

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
        return new AttachNetworkClusterValidator(interfaceDao, networkDao, getNetworkCluster(), getClusterVersion());
    }

    private Version getClusterVersion() {
        return getVdsGroup().getCompatibilityVersion();
    }

    private boolean logicalNetworkExists() {
        if (getPersistedNetwork() != null) {
            return true;
        }

        addCanDoActionMessage(EngineMessage.NETWORK_NOT_EXISTS);
        return false;
    }

    private boolean changesAreClusterCompatible() {
        if (!getParameters().getNetwork().isVmNetwork()) {
            if (!FeatureSupported.nonVmNetwork(getVdsGroup().getCompatibilityVersion())) {
                addCanDoActionMessage(EngineMessage.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
                return false;
            }
        }
        return true;
    }

    private boolean networkNotAttachedToCluster() {
        if (networkExists()) {
            return failCanDoAction(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_CLUSTER);
        }

        return true;
    }

    private boolean networkExists() {
        return getNetworkClusterDao().get(getNetworkCluster().getId()) != null;
    }

    private boolean vdsGroupExists() {
        if (!vdsGroupInDb()) {
            addCanDoActionMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        return true;
    }

    private boolean vdsGroupInDb() {
        return getVdsGroup() != null;
    }

    private void attachNetwork(Guid clusterId, NetworkCluster networkCluster, Network network) {
        getNetworkClusterDao().save(new NetworkCluster(clusterId, network.getId(),
                NetworkStatus.OPERATIONAL,
                false,
                networkCluster.isRequired(),
                false,
                false,
                false));

        List<VDS> hosts = vdsDao.getAllForVdsGroup(clusterId);
        List<Network> clusterNetworks = networkDao.getAllForCluster(clusterId);
        for (VDS host : hosts) {
            HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(this.networkAttachmentDao,
                host.getId(),
                interfaceDao.getAllInterfacesForVds(host.getId()),
                Collections.<NetworkAttachment> emptyList(),
                clusterNetworks);
            persister.persistNetworkAttachments();
        }

        if (network.getCluster().isDisplay()) {
            final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                    getNetworkClusterDao(),
                    getVmDao(),
                    networkCluster,
                    network.getName(),
                    auditLogDirector);
            if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
                displayNetworkClusterHelper.warnOnActiveVm();
            }

            getNetworkClusterDao().setNetworkExclusivelyAsDisplay(clusterId, network.getId());
        }

        if (network.getCluster().isMigration()) {
            getNetworkClusterDao().setNetworkExclusivelyAsMigration(clusterId, network.getId());
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
        addCanDoActionMessage(EngineMessage.VAR__TYPE__NETWORK);
        addCanDoActionMessage(EngineMessage.VAR__ACTION__ATTACH);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP
                             : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_GROUP_FAILED;
    }
}
