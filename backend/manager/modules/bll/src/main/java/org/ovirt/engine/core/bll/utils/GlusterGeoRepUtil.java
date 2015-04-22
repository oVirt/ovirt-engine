package org.ovirt.engine.core.bll.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.utils.linq.Predicate;

@Singleton
public class GlusterGeoRepUtil {

    public Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> getEligibilityPredicates(final GlusterVolumeEntity masterVolume) {
        Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicates = new HashMap<>();
        final List<Guid> existingSessionSlavesIds = getSessionSlaveVolumeIds();


        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_BE_UP, new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                return slaveVolume.getStatus() == GlusterStatus.UP;
            }
        });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER, new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                return ! masterVolume.getClusterId().equals(slaveVolume.getClusterId());
            }
        });

        final Predicate<GlusterVolumeEntity> nonNullSlaveSizePredicate = new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                return slaveVolume.getAdvancedDetails().getCapacityInfo() != null;
            }
        };
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE, nonNullSlaveSizePredicate);

        final Predicate<GlusterVolumeEntity> nonNullMasterSizePredicate = new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                return masterVolume.getAdvancedDetails().getCapacityInfo() != null;
            }
        };
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.MASTER_VOLUME_SIZE_TO_BE_AVAILABLE, nonNullMasterSizePredicate);

        Predicate<GlusterVolumeEntity> masterSlaveSizePredicate = new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                boolean eligible = nonNullSlaveSizePredicate.eval(slaveVolume) && nonNullMasterSizePredicate.eval(masterVolume);
                if (eligible) {
                    eligible = slaveVolume.getAdvancedDetails().getCapacityInfo().getTotalSize() >= masterVolume.getAdvancedDetails().getCapacityInfo().getTotalSize();
                }
                return eligible;
            }
        };
        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE, masterSlaveSizePredicate);

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION, new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                return !existingSessionSlavesIds.contains(slaveVolume.getId());
            }
        });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_CLUSTER_AND_MASTER_CLUSTER_COMPATIBILITY_VERSIONS_DO_NOT_MATCH, new Predicate<GlusterVolumeEntity>() {
            @Override
            public boolean eval(GlusterVolumeEntity slaveVolume) {
                VdsGroupDAO vdsGroupDao = getVdsGroupDao();
                Version slaveCompatibilityVersion = vdsGroupDao.get(slaveVolume.getClusterId()).getCompatibilityVersion();
                Version masterCompatibilityVersion = vdsGroupDao.get(masterVolume.getClusterId()).getCompatibilityVersion();
                return masterCompatibilityVersion.equals(slaveCompatibilityVersion);
            }
        });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.NO_UP_SLAVE_SERVER,
                new Predicate<GlusterVolumeEntity>() {
                    @Override
                    public boolean eval(GlusterVolumeEntity slaveVolume) {
                        Guid slaveUpserverId = getUpServerId(slaveVolume.getClusterId());
                        if (slaveUpserverId == null) {
                            return false;
                        }
                        return true;
                    }
                });

        eligibilityPredicates.put(GlusterGeoRepNonEligibilityReason.SLAVE_VOLUME_TO_BE_EMPTY,
                new Predicate<GlusterVolumeEntity>() {
                    @Override
                    public boolean eval(GlusterVolumeEntity slaveVolume) {
                        Guid slaveUpserverId = getUpServerId(slaveVolume.getClusterId());
                        if(slaveUpserverId == null) {
                            return false;
                        }
                        return checkEmptyGlusterVolume(slaveUpserverId, slaveVolume.getName());
                    }
                });

        return eligibilityPredicates;
    }

    private List<Guid> getSessionSlaveVolumeIds() {
        List<GlusterGeoRepSession> existingSessions = getGeoRepDao().getAllSessions();
        List<Guid> sessionSlavesIds = new ArrayList<Guid>();
        for(GlusterGeoRepSession currentSession : existingSessions) {
            sessionSlavesIds.add(currentSession.getSlaveVolumeId());
        }
        return sessionSlavesIds;
    }

    public boolean checkEmptyGlusterVolume(Guid slaveUpserverId, String slaveVolumeName) {
        VDSReturnValue returnValue =
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.CheckEmptyGlusterVolume,
                                new GlusterVolumeVDSParameters(slaveUpserverId, slaveVolumeName));
        if (!returnValue.getSucceeded()) {
            return false;
        }
        return (boolean) returnValue.getReturnValue();
    }

    public Guid getUpServerId(Guid clusterId) {
        VDS randomUpServer = ClusterUtils.getInstance().getRandomUpServer(clusterId);
        return randomUpServer == null ? null : randomUpServer.getId();
    }

    public VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    public GlusterGeoRepDao getGeoRepDao() {
        return DbFacade.getInstance().getGlusterGeoRepDao();
    }
}
