package org.ovirt.engine.core.bll.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@Singleton
public class GlusterGeoRepUtil {
    @Inject
    private GlusterUtil glusterUtil;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private GlusterGeoRepDao glusterGeoRepDao;

    @Inject
    private VDSBrokerFrontend resourceManager;

    public Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> getEligibilityPredicates(final GlusterVolumeEntity masterVolume) {
        Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicates = new HashMap<>();
        final List<Guid> existingSessionSlavesIds = getSessionSlaveVolumeIds();


        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_BE_UP, slaveVolume -> slaveVolume.getStatus() == GlusterStatus.UP);

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER, slaveVolume -> ! masterVolume.getClusterId().equals(slaveVolume.getClusterId()));

        final Predicate<GlusterVolumeEntity> nonNullSlaveSizePredicate = slaveVolume -> slaveVolume.getAdvancedDetails().getCapacityInfo() != null;
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE, nonNullSlaveSizePredicate);

        final Predicate<GlusterVolumeEntity> nonNullMasterSizePredicate = slaveVolume -> masterVolume.getAdvancedDetails().getCapacityInfo() != null;
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.MASTER_VOLUME_SIZE_TO_BE_AVAILABLE, nonNullMasterSizePredicate);

        Predicate<GlusterVolumeEntity> masterSlaveSizePredicate = slaveVolume -> {
            boolean eligible = nonNullSlaveSizePredicate.test(slaveVolume) && nonNullMasterSizePredicate.test(masterVolume);
            if (eligible) {
                eligible = slaveVolume.getAdvancedDetails().getCapacityInfo().getTotalSize() >= masterVolume.getAdvancedDetails().getCapacityInfo().getTotalSize();
            }
            return eligible;
        };
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE, masterSlaveSizePredicate);

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION, slaveVolume -> !existingSessionSlavesIds.contains(slaveVolume.getId()));

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_CLUSTER_AND_MASTER_CLUSTER_COMPATIBILITY_VERSIONS_DO_NOT_MATCH, slaveVolume -> {
            Version slaveCompatibilityVersion = clusterDao.get(slaveVolume.getClusterId()).getCompatibilityVersion();
            Version masterCompatibilityVersion = clusterDao.get(masterVolume.getClusterId()).getCompatibilityVersion();
            return masterCompatibilityVersion.equals(slaveCompatibilityVersion);
        });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.NO_UP_SLAVE_SERVER,
                slaveVolume -> {
                    Guid slaveUpserverId = getUpServerId(slaveVolume.getClusterId());
                    return slaveUpserverId != null;
                });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_TO_BE_EMPTY,
                slaveVolume -> {
                    Guid slaveUpserverId = getUpServerId(slaveVolume.getClusterId());
                    return slaveUpserverId != null && checkEmptyGlusterVolume(slaveUpserverId, slaveVolume.getName());
                });

        return eligibilityPredicates;
    }

    private List<Guid> getSessionSlaveVolumeIds() {
        List<GlusterGeoRepSession> existingSessions = glusterGeoRepDao.getAllSessions();
        return existingSessions.stream().map(GlusterGeoRepSession::getSlaveVolumeId).collect(Collectors.toList());
    }

    public boolean checkEmptyGlusterVolume(Guid slaveUpserverId, String slaveVolumeName) {
        VDSReturnValue returnValue =
                resourceManager
                        .runVdsCommand(VDSCommandType.CheckEmptyGlusterVolume,
                                new GlusterVolumeVDSParameters(slaveUpserverId, slaveVolumeName));
        return returnValue.getSucceeded() && (boolean) returnValue.getReturnValue();
    }

    public Guid getUpServerId(Guid clusterId) {
        VDS randomUpServer = glusterUtil.getRandomUpServer(clusterId);
        return randomUpServer == null ? null : randomUpServer.getId();
    }
}
