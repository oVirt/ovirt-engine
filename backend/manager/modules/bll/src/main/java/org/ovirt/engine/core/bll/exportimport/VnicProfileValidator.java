package org.ovirt.engine.core.bll.exportimport;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

public class VnicProfileValidator {
    private final VnicProfileDao vnicProfileDao;
    private final NetworkClusterDao networkClusterDao;

    @Inject
    VnicProfileValidator(VnicProfileDao vnicProfileDao, NetworkClusterDao networkClusterDao) {
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
        this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
    }

    public ValidationResult validateTargetVnicProfileId(Guid vnicProfileId, Guid clusterId) {
        final VnicProfile vnicProfile = vnicProfileDao.get(vnicProfileId);
        if (vnicProfile == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS);
        }

        final List<NetworkCluster> clusterNetworks = networkClusterDao.getAllForCluster(clusterId);
        final boolean networkIsFoundInCluster = clusterNetworks
                .stream()
                .map(NetworkCluster::getNetworkId)
                .anyMatch(vnicProfile.getNetworkId()::equals);
        return ValidationResult
                .failWith(EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER)
                .unless(networkIsFoundInCluster);
    }
}
