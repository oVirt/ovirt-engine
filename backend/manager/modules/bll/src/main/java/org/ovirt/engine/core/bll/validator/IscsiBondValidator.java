package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class IscsiBondValidator {
    @Inject
    private IscsiBondDao iscsiBondDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public ValidationResult iscsiBondWithTheSameNameExistInDataCenter(IscsiBond iscsiBond) {
        List<IscsiBond> iscsiBonds = iscsiBondDao.getAllByStoragePoolId(iscsiBond.getStoragePoolId());

        if (iscsiBonds.stream().anyMatch(bond -> bond.getName().equals(iscsiBond.getName()) &&
                !bond.getId().equals(iscsiBond.getId()))) {
            return new ValidationResult(EngineMessage.ISCSI_BOND_WITH_SAME_NAME_EXIST_IN_DATA_CENTER);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isIscsiBondExist(IscsiBond iscsiBond) {
        return (iscsiBond == null) ?
                new ValidationResult(EngineMessage.ISCSI_BOND_NOT_EXIST) : ValidationResult.VALID;
    }

    public ValidationResult validateAddedLogicalNetworks(IscsiBond iscsiBond) {
        return validateAddedLogicalNetworks(iscsiBond.getNetworkIds(), iscsiBond.getStoragePoolId());
    }

    public ValidationResult validateAddedLogicalNetworks(IscsiBond after, IscsiBond before) {
        Collection<Guid> addedLogicalNetworks = CollectionUtils.subtract(after.getNetworkIds(), before.getNetworkIds());
        return validateAddedLogicalNetworks(addedLogicalNetworks, after.getStoragePoolId());
    }

    public ValidationResult validateAddedStorageConnections(IscsiBond iscsiBond) {
        return validateAddedStorageConnections(iscsiBond.getStorageConnectionIds(), iscsiBond.getStoragePoolId());
    }

    public ValidationResult validateAddedStorageConnections(IscsiBond after, IscsiBond before) {
        Collection<String> addedStorageConnections =
                CollectionUtils.subtract(after.getStorageConnectionIds(), before.getStorageConnectionIds());
        return validateAddedStorageConnections(addedStorageConnections, after.getStoragePoolId());
    }

    private ValidationResult validateAddedLogicalNetworks(Collection<Guid> addedLogicalNetworks, Guid dataCenterId) {
        if (!addedLogicalNetworks.isEmpty()) {
            List<Guid> existingNetworks = getDataCenterLogicalNetworks(dataCenterId);
            Collection<Guid> missingNetworks = CollectionUtils.subtract(addedLogicalNetworks, existingNetworks);

            if (!missingNetworks.isEmpty()) {
                return new ValidationResult(EngineMessage.NETWORKS_DONT_EXIST_IN_DATA_CENTER,
                        String.format("$networkIds %s", StringUtils.join(missingNetworks, ",")),
                        String.format("$dataCenterId %s", dataCenterId));
            }

            Collection<Guid> requiredNetworks = getRequiredNetworks(addedLogicalNetworks);
            if (!requiredNetworks.isEmpty()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ISCSI_BOND_NETWORK_CANNOT_BE_REQUIRED);
            }
        }

        return ValidationResult.VALID;
    }

    private List<Guid> getDataCenterLogicalNetworks(Guid dataCenterId) {
        return networkDao.getAllForDataCenter(dataCenterId).stream().map(Network::getId).collect(Collectors.toList());
    }

    private List<Guid> getRequiredNetworks(Collection<Guid> addedLogicalNetworks) {
        return addedLogicalNetworks.stream().filter(this::isRequiredNetwork).collect(Collectors.toList());
    }

    private boolean isRequiredNetwork(Guid networkId) {
        return networkClusterDao.getAllForNetwork(networkId).stream().anyMatch(NetworkCluster::isRequired);
    }

    private ValidationResult validateAddedStorageConnections(Collection<String> addedStorageConnections, Guid dataCenterId) {
        if (!addedStorageConnections.isEmpty()) {
            List<String> connectionsNotInDataCenter = getStorageConnectionsMissingInDataCenter(addedStorageConnections, dataCenterId);
            if (!connectionsNotInDataCenter.isEmpty()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTIONS_CANNOT_BE_ADDED_TO_ISCSI_BOND,
                        String.format("$connectionIds %s", StringUtils.join(connectionsNotInDataCenter, ",")));
            }
        }

        return ValidationResult.VALID;
    }

    private List<String> getStorageConnectionsMissingInDataCenter(Collection<String> storageConnections, Guid dataCenterId) {
        Set<String> existingConnIds = storageServerConnectionDao
                .getConnectableStorageConnectionsByStorageType(dataCenterId, StorageType.ISCSI)
                .stream()
                .map(StorageServerConnections::getId)
                .collect(Collectors.toSet());

        return storageConnections.stream().filter(id -> !existingConnIds.contains(id)).collect(Collectors.toList());
    }
}
