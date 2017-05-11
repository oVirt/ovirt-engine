package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.ValidateSupportsTransaction;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.helper.DisplayNetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkAttachmentsPersister;

@InternalCommandAttribute
@ValidateSupportsTransaction
public class AttachNetworkToClusterInternalCommand<T extends AttachNetworkToClusterParameter>
    extends NetworkClusterCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private ClusterDao clusterDao;

    public AttachNetworkToClusterInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    @Override
    protected void executeCommand() {

        attachNetwork(getClusterId(), getNetworkCluster(), getNetwork());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        return networkNotAttachedToCluster()
                && clusterExists()
                && logicalNetworkExists()
                && validateAttachment();
    }

    private boolean validateAttachment() {
        final AttachNetworkClusterValidator attachNetworkClusterValidator = createNetworkClusterValidator();
        return validate(attachNetworkClusterValidator.networkBelongsToClusterDataCenter(getCluster(),
                getPersistedNetwork())) &&
               validateAttachment(attachNetworkClusterValidator);
    }

    private AttachNetworkClusterValidator createNetworkClusterValidator() {
        return new AttachNetworkClusterValidator(interfaceDao, networkDao, vdsDao, getNetworkCluster());
    }

    private boolean logicalNetworkExists() {
        if (getPersistedNetwork() != null) {
            return true;
        }

        addValidationMessage(EngineMessage.NETWORK_NOT_EXISTS);
        return false;
    }

    private boolean networkNotAttachedToCluster() {
        if (networkExists()) {
            return failValidation(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_CLUSTER);
        }

        return true;
    }

    private boolean networkExists() {
        return networkClusterDao.get(getNetworkCluster().getId()) != null;
    }

    private boolean clusterExists() {
        if (!clusterInDb()) {
            addValidationMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        return true;
    }

    private boolean clusterInDb() {
        return getCluster() != null;
    }

    private void attachNetwork(Guid clusterId, NetworkCluster networkClusterFromUserRequest, Network network) {
        networkClusterDao.save(new NetworkCluster(clusterId, network.getId(),
                NetworkStatus.OPERATIONAL,
                false,
                networkClusterFromUserRequest.isRequired(),
                false,
                false,
                false,
                false));

        List<VDS> hosts = vdsDao.getAllForCluster(clusterId);
        List<Network> networksOfCluster = networkDao.getAllForCluster(clusterId);
        for (VDS host : hosts) {
            HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(this.networkAttachmentDao,
                host.getId(),
                interfaceDao.getAllInterfacesForVds(host.getId()),
                Collections.emptyList(),
                networksOfCluster);
            persister.persistNetworkAttachments();
        }

        if (network.getCluster().isDisplay()) {
            final DisplayNetworkClusterHelper displayNetworkClusterHelper = new DisplayNetworkClusterHelper(
                    networkClusterDao,
                    vmDao,
                    clusterDao,
                    networkClusterFromUserRequest,
                    network.getName(),
                    auditLogDirector);
            if (displayNetworkClusterHelper.isDisplayToBeUpdated()) {
                displayNetworkClusterHelper.warnOnActiveVm();
            }

            networkClusterDao.setNetworkExclusivelyAsDisplay(clusterId, network.getId());
        }

        if (network.getCluster().isMigration()) {
            networkClusterDao.setNetworkExclusivelyAsMigration(clusterId, network.getId());
        }

        if (network.getCluster().isGluster()) {
            networkClusterDao.setNetworkExclusivelyAsGluster(clusterId, network.getId());
        }

        if (network.getCluster().isDefaultRoute()) {
            networkClusterDao.setNetworkExclusivelyAsDefaultRoute(clusterId, network.getId());
        }

        networkClusterHelper.setStatus(clusterId, network);
    }

    @Override
    protected String getDescription() {
        String networkName = getNetworkName() == null ? "" : getNetworkName();
        String clusterName = getCluster() == null ? "" : getCluster().getName();
        return networkName + " - " + clusterName;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
        addValidationMessage(EngineMessage.VAR__ACTION__ATTACH);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_CLUSTER
                             : AuditLogType.NETWORK_ATTACH_NETWORK_TO_CLUSTER_FAILED;
    }
}
