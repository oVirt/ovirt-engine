package org.ovirt.engine.core.bll.network.dc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;

@NonTransactiveCommandAttribute
public class AutodefineExternalNetworkCommand<T extends IdParameters> extends CommandBase<T> {

    private static final String EXTERNAL_NAME = "{0}{1}{2}";
    private static final String EXTERNAL_PREFIX = "external_";
    private static final String MULTIPLE_PROVIDERS = "_{0}";
    private static final String EXTERNAL_DESCRIPTION = "Auto-defined external network";

    @Inject
    private NetworkDao networkDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private NetworkClusterDao networkClusterDao;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private NetworkLocking networkLocking;

    private Network network;

    private Map<Guid, List<Guid>> clustersByProvider;

    public AutodefineExternalNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        List<Cluster> clustersWithOvs = getClustersWithOvsSwitchType(getNetwork().getId());
        logInvalidDefaultProvider(clustersWithOvs);
        List<Cluster> clustersWithProvider = getClustersWithValidDefaultProvider(clustersWithOvs);
        createListOfClustersByProvider(clustersWithProvider);

        List<Guid> providerIds = new ArrayList<>(clustersByProvider.keySet());
        IntStream.range(0, providerIds.size())
                .forEach(i -> runAutodefineForOneProvider(providerIds.get(i), i));
        // The operation has to succeed every time to prevent trigger of transactional rollback
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        NetworkValidator validator = new NetworkValidator(getNetwork());
        return validate(validator.networkIsSet(getParameters().getId()))
                && validate(validator.isVmNetwork())
                && validate(validator.notExternalNetwork());
    }

    private List<Cluster> getClustersWithOvsSwitchType(Guid networkId) {
        List<NetworkCluster> physicalNetworkClusters = networkClusterDao.getAllForNetwork(networkId);
        return physicalNetworkClusters.stream()
                .map(networkCluster -> clusterDao.get(networkCluster.getClusterId()))
                .filter(cluster -> cluster.hasRequiredSwitchType(SwitchType.OVS))
                .collect(Collectors.toList());
    }

    private List<Guid> getClusterIdsFilteredByProviderId(List<Cluster> clusters, Guid providerId) {
        return clusters.stream()
                .filter(cluster -> cluster.hasDefaultNetworkProviderId(providerId))
                .map(Cluster::getId)
                .collect(Collectors.toList());
    }

    private List<Cluster> getClustersWithValidDefaultProvider(List<Cluster> clusters) {
        return clusters.stream()
                .filter(Cluster::isSetDefaultNetworkProviderId)
                .collect(Collectors.toList());
    }

    private void logInvalidDefaultProvider(List<Cluster> clusters) {
        clusters.forEach(cluster -> {
            if (!cluster.isSetDefaultNetworkProviderId()) {
                auditLog(getWarningMessage(cluster), AuditLogType.NETWORK_AUTO_DEFINE_NO_DEFAULT_EXTERNAL_PROVIDER);
            }
        });
    }

    private void createListOfClustersByProvider(List<Cluster> clusters) {
        clustersByProvider = clusters.stream()
                .map(Cluster::getDefaultNetworkProviderId)
                .distinct()
                .collect(Collectors.toMap(providerId -> providerId,
                        providerId -> getClusterIdsFilteredByProviderId(clusters, providerId)));
    }

    // Acquire lock for every provider and run auto-define inside this lock
    private void runAutodefineForOneProvider(Guid providerId, int index) {
        try (EngineLock lock = acquireLockForProvider(providerId)) {
            Network externalNetwork = createExternalNetwork(calcExternalNetworkName(index), providerId);

            AddNetworkStoragePoolParameters storagePoolParameters =
                    new AddNetworkStoragePoolParameters(getNetwork().getStoragePoolId(), externalNetwork);
            storagePoolParameters.setNetworkClusterList(networkHelper.createNetworkClusters(clustersByProvider.get(
                    providerId)));
            storagePoolParameters.setVnicProfilePublicUse(false);
            runInternalAction(ActionType.AddNetworkOnProvider, storagePoolParameters);
        }
    }

    private Network createExternalNetwork(String name, Guid providerId) {
        final Network network = getNetwork();
        Network externalNetwork = new Network();
        externalNetwork.setName(name);
        externalNetwork.setDescription(EXTERNAL_DESCRIPTION);
        externalNetwork.setDataCenterId(network.getDataCenterId());
        externalNetwork.setMtu(NetworkUtils.getHostMtuActualValue(network));

        ProviderNetwork providerNetwork = new ProviderNetwork();
        providerNetwork.setProviderId(providerId);
        providerNetwork.setPhysicalNetworkId(network.getId());
        externalNetwork.setProvidedBy(providerNetwork);

        return externalNetwork;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }

    private Network getNetwork() {
        if (network == null) {
            network = networkDao.get(getParameters().getId());
        }
        return network;
    }

    private String calcExternalNetworkName(int index) {
        boolean multiple = clustersByProvider.keySet().size() > 1;

        String indexSuffix = multiple ? MessageFormat.format(MULTIPLE_PROVIDERS, index + 1) : "";
        String externalNetworkName = getNetwork().getName();

        int newNetworkNameLength = getNetwork().getName().length() + EXTERNAL_PREFIX.length() + indexSuffix.length();
        if (newNetworkNameLength > BusinessEntitiesDefinitions.NETWORK_NAME_SIZE) {
            externalNetworkName = externalNetworkName.substring(0,
                    BusinessEntitiesDefinitions.NETWORK_NAME_SIZE - EXTERNAL_PREFIX.length() - indexSuffix.length());
        }

        return MessageFormat.format(EXTERNAL_NAME, EXTERNAL_PREFIX, externalNetworkName, indexSuffix);
    }

    private AuditLogable getWarningMessage(Cluster cluster) {
        AuditLogable event = new AuditLogableImpl();
        event.setClusterName(cluster.getName());
        event.addCustomValue("NetworkName", getNetwork().getName());
        return event;
    }

    protected EngineLock acquireLockForProvider(Guid providerId) {
        EngineLock lock = new EngineLock(networkLocking.getNetworkProviderLock(providerId));
        lockManager.acquireLockWait(lock);
        return lock;
    }
}
