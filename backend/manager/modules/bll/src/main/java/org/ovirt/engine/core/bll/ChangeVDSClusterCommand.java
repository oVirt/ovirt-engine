package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkAttachmentsPersister;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ChangeVDSClusterCommand<T extends ChangeVDSClusterParameters> extends VdsCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;
    @Inject
    private ChangeClusterParametersBuilder changeClusterParametersBuilder;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private ClusterUtils clusterUtils;
    @Inject
    private HostedEngineHelper hostedEngineHelper;

    private StoragePool targetStoragePool;

    private Cluster targetCluster;

    private AuditLogType errorType = AuditLogType.USER_FAILED_UPDATE_VDS;

    private List<VdsNetworkInterface> hostNics;

    private List<Network> targetClusterNetworks;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId id of command
     */
    public ChangeVDSClusterCommand(Guid commandId) {
        super(commandId);
    }

    public ChangeVDSClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VDS vds = getVds();
        if (vds == null) {
            addValidationMessage(EngineMessage.VDS_INVALID_SERVER_ID);
            return false;
        }
        if (!ObjectIdentityChecker.canUpdateField(vds, "clusterId", vds.getStatus())) {
            addValidationMessage(EngineMessage.VDS_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        if (getTargetCluster() == null) {
            addValidationMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        targetStoragePool = storagePoolDao.getForCluster(getTargetCluster().getId());
        if (targetStoragePool != null && targetStoragePool.isLocal()) {
            if (!vdsStaticDao.getAllForCluster(getParameters().getClusterId()).isEmpty()) {
                addValidationMessage(EngineMessage.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
                return false;
            }
        }

        if (getCluster().supportsGlusterService()) {
            if (glusterDBUtils.hasBricks(getVdsId())) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                return false;
            }

            if (!hasUpServer(getSourceCluster())) {
                return false;
            }
        }

        if (getTargetCluster().supportsGlusterService() && !hasUpServerInTarget(getTargetCluster())) {
            return false;
        }

        vds.setCpuName(getCpuFlagsManagerHandler().
                findMaxServerCpuByFlags(vds.getCpuFlags(), getTargetCluster().getCompatibilityVersion()));

        // CPU flags are null if oVirt node cluster is changed during approve process.
        if (getTargetCluster().supportsVirtService() && !StringUtils.isEmpty(vds.getCpuFlags())) {
            if (vds.getCpuName() == null) {
                return failValidation(EngineMessage.CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION);
            }

            if (getTargetCluster().getArchitecture() != ArchitectureType.undefined &&
                    getTargetCluster().getArchitecture() != vds.getCpuName().getArchitecture()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_CLUSTER_DIFFERENT_ARCHITECTURES);
            }
        }

        if (!(VDSStatus.PendingApproval == vds.getStatus() || isDetachedSourceCluster() || isSameManagementNetwork())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_CLUSTER_DIFFERENT_MANAGEMENT_NETWORKS);
        }

        if (isHeHostBeingMovedToAnotherDC(vds)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_HOST_IN_ANOTHER_DC);
        }

        return true;
    }

    private boolean isHeHostBeingMovedToAnotherDC(VDS vds) {
        return hostedEngineHelper.isVmManaged() && vds.isHostedEngineDeployed()
                && !getTargetCluster().getStoragePoolId().equals(hostedEngineHelper.getStoragePoolId());
    }

    private boolean isDetachedSourceCluster() {
        return getSourceCluster().getStoragePoolId() == null;
    }

    private boolean isSameManagementNetwork() {
        final Network sourceManagementNetwork = networkDao.getManagementNetwork(getSourceCluster().getId());
        final Network targetManagementNetwork = networkDao.getManagementNetwork(getTargetCluster().getId());

        return targetManagementNetwork != null
                && sourceManagementNetwork.getName().equals(targetManagementNetwork.getName());
    }

    private List<VdsNetworkInterface> getHostNics() {
        if (hostNics == null) {
            hostNics = interfaceDao.getAllInterfacesForVds(getVdsId());
        }

        return hostNics;
    }

    private boolean hasUpServer(Cluster cluster) {
        if (clusterUtils.hasMultipleServers(cluster.getId())
                && glusterUtil.getUpServer(cluster.getId()) == null) {
            addNoUpServerMessage(cluster);
            return false;
        }
        return true;
    }

    private void addNoUpServerMessage(Cluster cluster) {
        addValidationMessageVariable("clusterName", cluster.getName());
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
    }

    private boolean hasUpServerInTarget(Cluster cluster) {
        if (clusterUtils.hasServers(cluster.getId())
                && glusterUtil.getUpServer(cluster.getId()) == null) {
            addNoUpServerMessage(cluster);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {

        final Guid targetClusterId = getParameters().getClusterId();
        if (getSourceCluster().getId().equals(targetClusterId)) {
            setSucceeded(true);
            return;
        }

        // save the new cluster id
        TransactionSupport.executeInNewTransaction(() -> {
            VdsStatic staticData = getVds().getStaticData();
            getCompensationContext().snapshotEntity(staticData);
            // if the new cluster's firewall type is different from the old cluster's firewall type, a reinstall
            // of the host is needed to reconfigure the firewall.
            if (targetCluster.getFirewallType() != getSourceCluster().getFirewallType()) {
                staticData.setReinstallRequired(true);
            }
            staticData.setClusterId(targetClusterId);
            vdsStaticDao.update(staticData);
            getCompensationContext().stateChanged();
            // remove the server from resource manager and add it back
            initializeVds();
            return null;
        });

        if (targetStoragePool != null
                && (getSourceCluster().getStoragePoolId()== null || !targetStoragePool.getId().equals(getSourceCluster().getStoragePoolId()))) {
            VdsActionParameters addVdsSpmIdParams = new VdsActionParameters(getVdsIdRef());
            addVdsSpmIdParams.setSessionId(getParameters().getSessionId());
            addVdsSpmIdParams.setCompensationEnabled(true);
            ActionReturnValue addVdsSpmIdReturn =
                    runInternalAction(ActionType.AddVdsSpmId,
                            addVdsSpmIdParams, cloneContext().withoutLock().withoutExecutionContext());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }

        if (getSourceCluster().supportsGlusterService() && clusterUtils.hasServers(getSourceCluster().getId())) {
            if (!glusterHostRemove(getSourceCluster().getId())) {
                setSucceeded(false);
                return;
            }
        }

        if (getTargetCluster().supportsGlusterService()
                && clusterUtils.hasMultipleServers(getTargetCluster().getId())) {
            if (!glusterHostAdd(getTargetCluster().getId())) {
                setSucceeded(false);
                return;
            }
        }

        if (getSourceCluster().getStoragePoolId() != null
                && (targetStoragePool== null || !getSourceCluster().getStoragePoolId().equals(targetStoragePool.getId()))) {
            vdsSpmIdMapDao.removeByVdsAndStoragePool(getVds().getId(), getSourceCluster().getStoragePoolId());
        }

        HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(this.networkAttachmentDao,
                getVdsId(),
                interfaceDao.getAllInterfacesForVds(getVdsId()),
                Collections.emptyList(),
                getTargetClusterNetworks());
        persister.persistNetworkAttachments();

        if (VDSStatus.PendingApproval != getVds().getStatus()) {
            configureNetworks();
        }

        if (!getSourceCluster().getRequiredSwitchTypeForCluster().equals(getTargetCluster().getRequiredSwitchTypeForCluster())) {
            runInternalAction(ActionType.SyncAllHostNetworks, new PersistentHostSetupNetworksParameters(getVdsId()));
        }

        setSucceeded(true);
    }

    private List<Network> getTargetClusterNetworks() {
        if (targetClusterNetworks == null) {
            targetClusterNetworks = networkDao.getAllForCluster(getTargetCluster().getId());
        }

        return targetClusterNetworks;
    }

    private void configureNetworks() {
        final PersistentHostSetupNetworksParameters params;

        try {
            params = changeClusterParametersBuilder.buildParameters(getVdsId(), getSourceCluster().getId(), getTargetCluster().getId());
        } catch (EngineException e) {
            auditLogDirector.log(this,
                    AuditLogType.CONFIGURE_NETWORK_BY_LABELS_WHEN_CHANGING_CLUSTER_FAILED);
            return;
        }

        ThreadPoolUtil.execute(() -> runInternalAction(
                ActionType.PersistentHostSetupNetworks,
                params,
                cloneContextAndDetachFromParent()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS : errorType;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        VdsDynamic vdsDynamic = getVds().getDynamicData();

        // If the state of the host is PendingApproval then we just check if the user has a permission on the destination cluster
        // Otherwise we require adding this permission both on the host and on the cluster, and it is not really needed
        // in order to approve a host
        if (vdsDynamic != null && !VDSStatus.PendingApproval.equals(vdsDynamic.getStatus())) {
            permissionList.add(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS, getActionType().getActionGroup()));
        }

        permissionList.add(new PermissionSubject(getParameters().getClusterId(), VdcObjectType.Cluster, getActionType().getActionGroup()));
        return Collections.unmodifiableList(permissionList);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    private boolean glusterHostRemove(Guid sourceClusterId) {
        // If "gluster peer detach" and "gluster peer status" are executed simultaneously, the results
        // are unpredictable. Hence locking the cluster to ensure the sync job does not lead to race
        // condition.
        try (EngineLock lock = glusterUtil.acquireGlusterLockWait(sourceClusterId)) {
            VDS runningHostInSourceCluster = glusterUtil.getUpServer(sourceClusterId);
            if (runningHostInSourceCluster == null) {
                log.error("Cannot remove host from source cluster, no host in Up status found in source cluster");
                handleError(-1, "No host in Up status found in source cluster");
                errorType = AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
                return false;
            }
            VDSReturnValue returnValue =
                    runVdsCommand(
                            VDSCommandType.RemoveGlusterServer,
                            new RemoveGlusterServerVDSParameters(runningHostInSourceCluster.getId(),
                                    getVds().getHostName(),
                                    false));
            if (!returnValue.getSucceeded()) {
                handleVdsError(returnValue);
                errorType = AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
                return false;
            }
            return true;
        }
    }

    private boolean glusterHostAdd(Guid targetClusterId) {
        // If "gluster peer probe" and "gluster peer status" are executed simultaneously, the results
        // are unpredictable. Hence locking the cluster to ensure the sync job does not lead to race
        // condition.
        try (EngineLock lock = glusterUtil.acquireGlusterLockWait(targetClusterId)) {
            VDS runningHostInTargetCluster = glusterUtil.getUpServer(targetClusterId);
            if (runningHostInTargetCluster == null) {
                log.error("Cannot add host to target cluster, no host in Up status found in target cluster");
                handleError(-1, "No host in Up status found in target cluster");
                errorType = AuditLogType.GLUSTER_SERVER_ADD_FAILED;
                return false;
            }
            VDSReturnValue returnValue =
                    runVdsCommand(
                            VDSCommandType.AddGlusterServer,
                            new AddGlusterServerVDSParameters(runningHostInTargetCluster.getId(),
                                    getVds().getHostName()));
            if (!returnValue.getSucceeded()) {
                handleVdsError(returnValue);
                errorType = AuditLogType.GLUSTER_SERVER_ADD_FAILED;
                return false;
            }
            return true;
        }
    }

    private void handleError(int errorCode, String errorMsg) {
        getReturnValue().getFault().setError(errorCode);
        getReturnValue().getFault().setMessage(errorMsg);
        getReturnValue().getExecuteFailedMessages().add(errorMsg);
    }

    private Cluster getSourceCluster() {
        return getCluster();
    }

    private Cluster getTargetCluster() {
        if (targetCluster == null) {
            targetCluster = clusterDao.get(getParameters().getClusterId());
        }
        return targetCluster;
    }

    private static class ChangeClusterParametersBuilder extends HostSetupNetworksParametersBuilder {

        @Inject
        NetworkDao networkDao;

        @Inject
        public ChangeClusterParametersBuilder(InterfaceDao interfaceDao,
                VdsStaticDao vdsStaticDao,
                NetworkClusterDao networkClusterDao,
                NetworkAttachmentDao networkAttachmentDao) {
            super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
        }

        public PersistentHostSetupNetworksParameters buildParameters(Guid hostId,
                Guid sourceClusterId,
                Guid targetClusterId) {
            List<Network> targetClusterNetworks = networkDao.getAllForCluster(targetClusterId);
            Map<String, Network> targetClusterNetworksByName = Entities.entitiesByName(targetClusterNetworks);

            PersistentHostSetupNetworksParameters params = createHostSetupNetworksParameters(hostId);
            Map<String, VdsNetworkInterface> nicsByNetwork =
                    NetworkUtils.hostInterfacesByNetworkName(getNics(hostId));
            Map<String, List<Network>> targetNetworksByLabel = getClusterNetworksByLabel(targetClusterNetworks);
            Map<String, List<Network>> sourceNetworksByLabel =
                    getClusterNetworksByLabel(networkDao.getAllForCluster(sourceClusterId));

            // Detect which networks should be added and which should be removed
            for (VdsNetworkInterface nic : getNics(hostId)) {
                adjustNetworksByLabel(sourceNetworksByLabel,
                        targetClusterNetworksByName,
                        targetNetworksByLabel,
                        params,
                        nicsByNetwork,
                        nic);
            }

            return params;
        }

        /**
         * Add or remove labeled networks from a nic by the assignment of networks to the target cluster:
         * <ul>
         * <li>Assigned labeled network will be defined on the nic if isn't already</li>
         * <li>Unassigned labeled network will be removed from a nic if isn't already</li>
         * </ul>
         *
         * @param sourceNetworksByLabel
         *            a map of the labeled source cluster networks by their label
         * @param targetNetworksByName
         *            a map of the destination cluster networks by their name
         * @param targetNetworksByLabel
         *            a map of the labeled target cluster networks by their label
         * @param params
         *            the setup networks parameters to be adjusted according to the findings
         * @param nicsByNetwork
         *            a map of nics by the network attached to them
         * @param nic
         *            the current examined network interface
         */
        private void adjustNetworksByLabel(Map<String, List<Network>> sourceNetworksByLabel,
                Map<String, Network> targetNetworksByName,
                Map<String, List<Network>> targetNetworksByLabel,
                PersistentHostSetupNetworksParameters params,
                Map<String, VdsNetworkInterface> nicsByNetwork,
                VdsNetworkInterface nic) {
            if (!NetworkUtils.isLabeled(nic)) {
                return;
            }

            for (String label : nic.getLabels()) {
                removeNetworksNoLongerAttachedViaLabel(sourceNetworksByLabel,
                        targetNetworksByName,
                        params,
                        nicsByNetwork,
                        nic,
                        label);

                addNetworksThatShouldBeAttachedViaLabel(targetNetworksByName,
                        targetNetworksByLabel,
                        params,
                        nicsByNetwork,
                        nic,
                        label);
            }
        }

        private void addNetworksThatShouldBeAttachedViaLabel(Map<String, Network> targetNetworksByName,
                Map<String, List<Network>> targetNetworksByLabel,
                PersistentHostSetupNetworksParameters params,
                Map<String, VdsNetworkInterface> nicsByNetwork,
                VdsNetworkInterface nic,
                String label) {
            // configure networks by target cluster assignment
            List<Network> targetLabeledNetworks = targetNetworksByLabel.get(label);
            if (targetLabeledNetworks != null) {
                for (Network net : targetLabeledNetworks) {
                    if (targetNetworksByName.containsKey(net.getName())
                            && !isNetworkAssignedToNic(nic, net.getName(), nicsByNetwork)) {
                        addAttachmentToParameters(nic, net, params);
                    }
                }
            }
        }

        private void removeNetworksNoLongerAttachedViaLabel(Map<String, List<Network>> sourceNetworksByLabel,
                Map<String, Network> targetNetworksByName,
                PersistentHostSetupNetworksParameters params,
                Map<String, VdsNetworkInterface> nicsByNetwork,
                VdsNetworkInterface nic,
                String label) {
            List<Network> sourceLabeledNetworks = sourceNetworksByLabel.get(label);

            if (sourceLabeledNetworks != null) {
                for (Network sourceLabeledNetwork : sourceLabeledNetworks) {
                    String networkName = sourceLabeledNetwork.getName();
                    if (isNetworkAssignedToNic(nic, networkName, nicsByNetwork)) {
                        // The network was attached to the nic via label (in the source cluster)
                        if (!isNetworkAssignedToTargetCluster(targetNetworksByName, networkName)) {
                            // the network is not attached to the target cluster- should be removed from the host
                            params.getRemovedUnmanagedNetworks().add(networkName);
                        } else if (!isNetworkLabelExistInTargetHost(nic.getVdsId(), networkName, targetNetworksByName)) {
                            // the target network doesn't have label that exist on the host
                            Network targetNetwork = targetNetworksByName.get(networkName);
                            NetworkAttachment attachment = getNetworkIdToAttachmentMap(nic.getVdsId()).get(targetNetwork.getId());
                            params.getRemovedNetworkAttachments().add(attachment.getId());
                        }
                    }
                }
            }
        }

        private boolean isNetworkAssignedToTargetCluster(Map<String, Network> targetNetworksByName,
                String networkName) {
            return targetNetworksByName.containsKey(networkName);
        }

        private boolean isNetworkAssignedToNic(VdsNetworkInterface nic,
                String networkName,
                Map<String, VdsNetworkInterface> nicsByNetwork) {
            VdsNetworkInterface nicToWhichNetworkIsAssigned = nicsByNetwork.get(networkName);
            String baseNicNameToWhichNetworkIsAssigned =
                    nicToWhichNetworkIsAssigned == null ? null : NetworkCommonUtils.stripVlan(nicToWhichNetworkIsAssigned);
            return nic.getName().equals(baseNicNameToWhichNetworkIsAssigned);

        }

        private boolean isNetworkLabelExistInTargetHost(Guid hostId, String networkName, Map<String, Network> targetNetworksByName) {
            Network targetNetwork = targetNetworksByName.get(networkName);
            return NetworkUtils.isLabeled(targetNetwork) && isHostContainLabel(hostId, targetNetwork.getLabel());
        }

        private boolean isHostContainLabel(Guid hostId, String label) {
            for (VdsNetworkInterface nic : getNics(hostId)) {
                if (NetworkUtils.isLabeled(nic) && nic.getLabels().contains(label)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Returns a map of labels to the cluster networks represented by that label.
         */
        private Map<String, List<Network>> getClusterNetworksByLabel(List<Network> clusterNetworks) {
            Map<String, List<Network>> networksByLabel = new HashMap<>();
            for (Network network : clusterNetworks) {
                if (NetworkUtils.isLabeled(network)) {
                    if (!networksByLabel.containsKey(network.getLabel())) {
                        networksByLabel.put(network.getLabel(), new ArrayList<>());
                    }

                    networksByLabel.get(network.getLabel()).add(network);
                }
            }

            return networksByLabel;
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }
}
