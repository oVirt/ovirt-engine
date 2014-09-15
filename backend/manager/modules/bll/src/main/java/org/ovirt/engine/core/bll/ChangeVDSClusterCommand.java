package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.NetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ChangeVDSClusterCommand<T extends ChangeVDSClusterParameters> extends VdsCommand<T> {

    private StoragePool targetStoragePool;

    private VDSGroup targetCluster;

    private AuditLogType errorType = AuditLogType.USER_FAILED_UPDATE_VDS;

    private List<VdsNetworkInterface> hostNics;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    public ChangeVDSClusterCommand(Guid commandId) {
        super(commandId);
    }

    public ChangeVDSClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        VDS vds = getVds();
        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            return false;
        }
        if (!ObjectIdentityChecker.CanUpdateField(vds, "vdsGroupId", vds.getStatus())) {
            addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        if (getTargetCluster() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        targetStoragePool = DbFacade.getInstance().getStoragePoolDao().getForVdsGroup(getTargetCluster().getId());
        if (targetStoragePool != null && targetStoragePool.isLocal()) {
            if (!DbFacade.getInstance().getVdsStaticDao().getAllForVdsGroup(getParameters().getClusterId()).isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
                return false;
            }
        }

        if (getVdsGroup().supportsGlusterService()) {
            if (getGlusterUtils().hasBricks(getVdsId())) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                return false;
            }

            if (!hasUpServer(getSourceCluster())) {
                return false;
            }
        }

        if (getTargetCluster().supportsGlusterService() && !hasUpServerInTarget(getTargetCluster())) {
            return false;
        }

        vds.setCpuName(CpuFlagsManagerHandler.FindMaxServerCpuByFlags(vds.getCpuFlags(),
                getTargetCluster().getcompatibility_version()));


        // CPU flags are null if oVirt node cluster is changed during approve process.
        if (getTargetCluster().supportsVirtService() && !StringUtils.isEmpty(vds.getCpuFlags())) {
            if (vds.getCpuName() == null) {
                return failCanDoAction(VdcBllMessages.CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION);
            }

            if (getTargetCluster().getArchitecture() != ArchitectureType.undefined &&
                    getTargetCluster().getArchitecture() != vds.getCpuName().getArchitecture()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_CLUSTER_DIFFERENT_ARCHITECTURES);
            }
        }

        if (FeatureSupported.hostNetworkQos(getSourceCluster().getcompatibility_version())
                && !FeatureSupported.hostNetworkQos(getTargetCluster().getcompatibility_version())) {
            for (VdsNetworkInterface iface : getHostNics()) {
                if (iface.getQos() != null) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED,
                            String.format("$ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED_LIST %s",
                                    iface.getNetworkName()));
                }
            }
        }

        if (FeatureSupported.networkCustomProperties(getSourceCluster().getcompatibility_version())
                && !FeatureSupported.networkCustomProperties(getTargetCluster().getcompatibility_version())) {
            for (VdsNetworkInterface iface : getHostNics()) {
                if (iface.hasCustomProperties()) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED,
                            String.format("$ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED_LIST %s",
                                    iface.getNetworkName()));
                }
            }
        }

        if (!targetClusterSupportsSetupNetworks() && hostHasLabeledNics()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_LABELS_NOT_SUPPORTED);
        }

        return true;
    }

    private boolean hostHasLabeledNics() {
        for (VdsNetworkInterface nic : getHostNics()) {
            if (NetworkUtils.isLabeled(nic)) {
                return true;
            }
        }

        return false;
    }

    private List<VdsNetworkInterface> getHostNics() {
        if (hostNics == null) {
            hostNics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getVdsId());
        }

        return hostNics;
    }

    private boolean hasUpServer(VDSGroup cluster) {
        if (getClusterUtils().hasMultipleServers(cluster.getId())
                && getClusterUtils().getUpServer(cluster.getId()) == null) {
            addNoUpServerMessage(cluster);
            return false;
        }
        return true;
    }

    private void addNoUpServerMessage(VDSGroup cluster) {
        addCanDoActionMessageVariable("clusterName", cluster.getName());
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
    }

    private boolean hasUpServerInTarget(VDSGroup cluster) {
        if (getClusterUtils().hasServers(cluster.getId())
                && getClusterUtils().getUpServer(cluster.getId()) == null) {
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
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                VdsStatic staticData = getVds().getStaticData();
                getCompensationContext().snapshotEntity(staticData);
                staticData.setVdsGroupId(targetClusterId);
                DbFacade.getInstance().getVdsStaticDao().update(staticData);
                getCompensationContext().stateChanged();
                // remove the server from resource manager and add it back
                initializeVds();
                return null;
            }
        });

        if (targetStoragePool != null
                && (getSourceCluster().getStoragePoolId()== null || !targetStoragePool.getId().equals(getSourceCluster().getStoragePoolId()))) {
            VdsActionParameters addVdsSpmIdParams = new VdsActionParameters(getVdsIdRef());
            addVdsSpmIdParams.setSessionId(getParameters().getSessionId());
            addVdsSpmIdParams.setCompensationEnabled(true);
            VdcReturnValueBase addVdsSpmIdReturn =
                    runInternalAction(VdcActionType.AddVdsSpmId,
                            addVdsSpmIdParams, cloneContext().withoutLock().withoutExecutionContext());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }


        if (getSourceCluster().supportsGlusterService() && getClusterUtils().hasServers(getSourceCluster().getId())) {
            if (!glusterHostRemove(getSourceCluster().getId())) {
                setSucceeded(false);
                return;
            }
        }

        if (getTargetCluster().supportsGlusterService()
                && getClusterUtils().hasMultipleServers(getTargetCluster().getId())) {
            if (!glusterHostAdd(getTargetCluster().getId())) {
                setSucceeded(false);
                return;
            }
        }

        if (getSourceCluster().getStoragePoolId() != null
                && (targetStoragePool== null || !getSourceCluster().getStoragePoolId().equals(targetStoragePool.getId()))) {
            getVdsSpmIdMapDAO().removeByVdsAndStoragePool(getVds().getId(), getSourceCluster().getStoragePoolId());
        }

        if (targetClusterSupportsSetupNetworks()) {
            configureNetworks();
        }

        setSucceeded(true);
    }

    private boolean targetClusterSupportsSetupNetworks() {
        return NetworkHelper.setupNetworkSupported(getTargetCluster().getcompatibility_version());
    }

    private void configureNetworks() {
        ChangeClusterParametersBuilder builder = new ChangeClusterParametersBuilder(getContext());
        final PersistentSetupNetworksParameters params;

        try {
            params = builder.buildParameters(getVdsId(), getSourceCluster().getId(), getTargetCluster().getId());
        } catch (VdcBLLException e) {
            AuditLogDirector.log(new AuditLogableBase(getVdsId()),
                    AuditLogType.CONFIGURE_NETWORK_BY_LABELS_WHEN_CHANGING_CLUSTER_FAILED);
            return;
        }

        ThreadPoolUtil.execute(new Runnable() {

            @Override
            public void run() {
                runInternalAction(VdcActionType.PersistentSetupNetworks, params, cloneContextAndDetachFromParent());

            }
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS : errorType;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        VdsDynamic vdsDynamic = getVds().getDynamicData();

        // If the state of the host is PendingApproval then we just check if the user has a permission on the destination cluster
        // Otherwise we require adding this permission both on the host and on the cluster, and it is not really needed
        // in order to approve a host
        if (vdsDynamic != null && !VDSStatus.PendingApproval.equals(vdsDynamic.getStatus())) {
            permissionList.add(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS, getActionType().getActionGroup()));
        }

        permissionList.add(new PermissionSubject(getParameters().getClusterId(), VdcObjectType.VdsGroups, getActionType().getActionGroup()));
        List<PermissionSubject> unmodifiableList = Collections.unmodifiableList(permissionList);
        return unmodifiableList;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    private boolean glusterHostRemove(Guid sourceClusterId) {
        // If "gluster peer detach" and "gluster peer status" are executed simultaneously, the results
        // are unpredictable. Hence locking the cluster to ensure the sync job does not lead to race
        // condition.
        try (EngineLock lock = GlusterUtil.getInstance().acquireGlusterLockWait(sourceClusterId)) {
            String hostName =
                    (getVds().getHostName().isEmpty()) ? getVds().getManagementIp()
                            : getVds().getHostName();
            VDS runningHostInSourceCluster = getClusterUtils().getUpServer(sourceClusterId);
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
                                    hostName,
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
        try (EngineLock lock = GlusterUtil.getInstance().acquireGlusterLockWait(targetClusterId)) {
            String hostName =
                    (getVds().getHostName().isEmpty()) ? getVds().getManagementIp()
                            : getVds().getHostName();
            VDS runningHostInTargetCluster = getClusterUtils().getUpServer(targetClusterId);
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
                                    hostName));
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

    private ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    private GlusterDBUtils getGlusterUtils() {
        return GlusterDBUtils.getInstance();
    }

    private VDSGroup getSourceCluster() {
        return getVdsGroup();
    }

    private VDSGroup getTargetCluster() {
        if (targetCluster == null) {
            targetCluster = DbFacade.getInstance().getVdsGroupDao().get(getParameters().getClusterId());
        }
        return targetCluster;
    }

    private class ChangeClusterParametersBuilder extends NetworkParametersBuilder {

        public ChangeClusterParametersBuilder(CommandContext commandContext) {
            super(commandContext);
        }

        public PersistentSetupNetworksParameters buildParameters(Guid hostId, Guid sourceClusterId, Guid targetClusterId) {
            List<Network> targetClusterNetworks = getNetworkDAO().getAllForCluster(targetClusterId);
            Map<String, Network> targetClusterNetworksByName = Entities.entitiesByName(targetClusterNetworks);

            PersistentSetupNetworksParameters params = createSetupNetworksParameters(hostId);
            Map<String, VdsNetworkInterface> nicsByNetwork =
                    Entities.hostInterfacesByNetworkName(params.getInterfaces());
            Map<String, List<Network>> targetNetworksByLabel = getClusterNetworksByLabel(targetClusterNetworks);
            Map<String, List<Network>> sourceNetworksByLabel =
                    getClusterNetworksByLabel(getNetworkDAO().getAllForCluster(sourceClusterId));
            List<VdsNetworkInterface> hostNics = new ArrayList<>(params.getInterfaces());

            // Detect which networks should be added and which should be removed
            for (VdsNetworkInterface nic : hostNics) {
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
        public void adjustNetworksByLabel(Map<String, List<Network>> sourceNetworksByLabel,
                Map<String, Network> targetNetworksByName,
                Map<String, List<Network>> targetNetworksByLabel,
                PersistentSetupNetworksParameters params,
                Map<String, VdsNetworkInterface> nicsByNetwork,
                VdsNetworkInterface nic) {
            if (!NetworkUtils.isLabeled(nic)) {
                return;
            }

            for (String label : nic.getLabels()) {
                // remove labeled networks originated in source cluster but not assigned to target cluster
                List<Network> sourceLabeledNetworks = sourceNetworksByLabel.get(label);
                if (sourceLabeledNetworks != null) {
                    for (Network net : sourceLabeledNetworks) {
                        if (configuredNetworkNotAssignedToCluster(targetNetworksByName, nicsByNetwork, net)) {
                            removeNetworkFromParameters(params, nic, net);
                        }
                    }
                }

                // configure networks by target cluster assignment
                List<Network> targetLabeledNetworks = targetNetworksByLabel.get(label);
                if (targetLabeledNetworks != null) {
                    for (Network net : targetLabeledNetworks) {
                        if (targetNetworksByName.containsKey(net.getName())
                                && !nicsByNetwork.containsKey(net.getName())) {
                            configureNetwork(nic, params.getInterfaces(), net);
                        } else if (configuredNetworkNotAssignedToCluster(targetNetworksByName, nicsByNetwork, net)) {
                            removeNetworkFromParameters(params, nic, net);
                        }
                    }
                }
            }
        }

        private boolean configuredNetworkNotAssignedToCluster(Map<String, Network> targetNetworksByName,
                Map<String, VdsNetworkInterface> nicsByNetwork,
                Network net) {
            return nicsByNetwork.containsKey(net.getName()) && !targetNetworksByName.containsKey(net.getName());
        }

        public void removeNetworkFromParameters(PersistentSetupNetworksParameters params,
                VdsNetworkInterface nic,
                Network net) {
            if (NetworkUtils.isVlan(net)) {
                VdsNetworkInterface vlan = getVlanDevice(params.getInterfaces(), nic, net);
                if (vlan == null) {
                    throw new VdcBLLException(VdcBllErrors.NETWORK_LABEL_CONFLICT);
                } else {
                    params.getInterfaces().remove(vlan);
                }
            } else if (StringUtils.equals(net.getName(), nic.getNetworkName())) {
                nic.setNetworkName(null);
            }
        }

        /**
         * Returns a map of labels to the cluster networks represented by that label.
         */
        private Map<String, List<Network>> getClusterNetworksByLabel(List<Network> clusterNetworks) {
            Map<String, List<Network>> networksByLabel = new HashMap<>();
            for (Network network : clusterNetworks) {
                if (NetworkUtils.isLabeled(network)) {
                    if (!networksByLabel.containsKey(network.getLabel())) {
                        networksByLabel.put(network.getLabel(), new ArrayList<Network>());
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
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
        locks.put(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }
}
