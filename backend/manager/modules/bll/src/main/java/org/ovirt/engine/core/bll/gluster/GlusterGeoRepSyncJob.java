package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.ovirt.engine.core.bll.scheduling.OnTimerMethodAnnotation;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterSchedulerDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterGeoRepSyncJob extends GlusterJob {
    private static final Logger log = LoggerFactory.getLogger(GlusterGeoRepSyncJob.class);

    @Inject
    private GlusterSchedulerDao scheduleDao;

    private static final GeoRepSessionStatus[] overridableStatuses = { GeoRepSessionStatus.ACTIVE,
            GeoRepSessionStatus.INITIALIZING,
            GeoRepSessionStatus.CREATED
    };

    @Override
    public Collection<GlusterJobSchedulingDetails> getSchedulingDetails() {
        return Arrays.asList(
                new GlusterJobSchedulingDetails(
                        "gluster_georep_poll_event",
                        getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepDiscoveryInSecs)),
                new GlusterJobSchedulingDetails(
                        "gluster_georepstatus_poll_event",
                        getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepStatusInSecs))
                );
    }

    @OnTimerMethodAnnotation("gluster_georep_poll_event")
    public void discoverGeoRepData() {
        // get all clusters
        List<Cluster> clusters = clusterDao.getAll();
        // for every cluster that supports geo-rep monitoring
        for (Cluster cluster : clusters) {
            discoverGeoRepDataInCluster(cluster);
        }

    }

    @OnTimerMethodAnnotation("gluster_georepstatus_poll_event")
    public void refreshGeoRepSessionStatus() {
        // get all clusters
        List<Cluster> clusters = clusterDao.getAll();
        // for every cluster that supports geo-rep monitoring
        for (Cluster cluster : clusters) {
            refreshGeoRepSessionStatusInCluster(cluster);
        }
    }

    private void refreshGeoRepSessionStatusInCluster(final Cluster cluster) {
        if (!supportsGlusterGeoRepFeature(cluster)) {
            return;
        }
        List<GlusterGeoRepSession> geoRepSessions = geoRepDao.getGeoRepSessionsInCluster(cluster.getId());
        refreshGeoRepSessionStatusForSessions(cluster, geoRepSessions);
    }

    /**
     * Exposing this to be called via BLL command in case of force sync of geo-replication session data for volume
     */
    public void refreshGeoRepDataForVolume(final GlusterVolumeEntity volume) {
        if (volume == null) {
            throw new EngineException(EngineError.GlusterVolumeGeoRepSyncFailed, "No volume information");
        }
        Cluster cluster = clusterDao.get(volume.getClusterId());
        discoverGeoRepDataInCluster(cluster, volume);
        List<GlusterGeoRepSession> geoRepSessions = geoRepDao.getGeoRepSessions(volume.getId());
        refreshGeoRepSessionStatusForSessions(cluster, geoRepSessions);
    }

    private void refreshGeoRepSessionStatusForSessions(final Cluster cluster, List<GlusterGeoRepSession> geoRepSessions) {
        if (CollectionUtils.isEmpty(geoRepSessions)) {
            return;
        }
        List<Callable<GlusterGeoRepSession>> geoRepSessionCalls = new ArrayList<>();
        for (final GlusterGeoRepSession geoRepSession: geoRepSessions) {
            geoRepSessionCalls.add(() -> {
                geoRepSession.setSessionDetails((ArrayList) getSessionDetailFromCLI(cluster, geoRepSession));
                return geoRepSession;
            });
        }

        List<GlusterGeoRepSession> updatedSessions = ThreadPoolUtil.invokeAll(geoRepSessionCalls);
        for (GlusterGeoRepSession updatedSession : updatedSessions) {
            if (updatedSession.getSessionDetails() == null) {
                log.info("Geo-replication session details not updated for session '{}' as there was error returning data from VDS",
                        updatedSession.getSessionKey());
                continue;
            }
            try (EngineLock lock = acquireGeoRepSessionLock(updatedSession.getId())) {
                GlusterVolumeEntity masterVolume = volumeDao.getById(updatedSession.getMasterVolumeId());
                updateGeoRepStatus(masterVolume, updatedSession);
                geoRepDao.updateSession(updatedSession);
                updateSessionDetailsInDB(updatedSession);
            } catch (Exception e) {
                log.error("Error updating session details '{}' : '{}'", updatedSession.getSessionKey(), e.getMessage());
                log.debug("Exception", e);
            }
        }
    }

    private void discoverGeoRepDataInCluster(Cluster cluster) {
        discoverGeoRepDataInCluster(cluster, null);
    }

    private void discoverGeoRepDataInCluster(Cluster cluster, GlusterVolumeEntity volume) {
        if (!supportsGlusterGeoRepFeature(cluster)) {
            return;
        }

        Map<String, GlusterGeoRepSession> sessionsMap = getSessionsFromCLI(cluster, volume);
        if (sessionsMap == null) {
            log.debug("No sessions retrieved for cluster: {} from CLI, nothing to do", cluster.getName());
            return;
        }

        updateDiscoveredSessions(cluster, sessionsMap, volume);
    }

    private void updateDiscoveredSessions(Cluster cluster, Map<String, GlusterGeoRepSession> sessionsMap,
            GlusterVolumeEntity volume) {
        removeDeletedSessions(cluster, sessionsMap, volume);

        // for each geo-rep session, find session in database and update details.
        for (GlusterGeoRepSession session : sessionsMap.values()) {
            GlusterVolumeEntity masterVolume = getVolume(cluster, session.getMasterVolumeName());
            if (masterVolume == null) {
                log.info("Could not find corresponding volume for geo-rep session '{}' and volume '{}' - status will not be updated.",
                        session.getSessionKey(),
                        session.getMasterVolumeName());
            } else {
                session.setMasterVolumeId(masterVolume.getId());
                // update consolidated status
                updateGeoRepStatus(masterVolume, session);
            }

            // check if session exists in database
            GlusterGeoRepSession sessionInDb = geoRepDao.getGeoRepSession(session.getSessionKey());
            if (sessionInDb == null) {
                // save the session in database first.
                log.debug("detected new geo-rep session '{}' for volume '{}'",
                        session.getSessionKey(),
                        session.getMasterVolumeName());
                if (Guid.isNullOrEmpty(session.getId())) {
                    session.setId(Guid.newGuid());
                }
                if (session.getSlaveNodeUuid() == null && session.getSlaveVolumeId() == null) {
                    updateSlaveNodeAndVolumeId(session);
                }
                geoRepDao.save(session);
                logGeoRepMessage(AuditLogType.GLUSTER_GEOREP_SESSION_DETECTED_FROM_CLI, cluster, session);
            } else {
                // if retrieved session does not have the slave uuid's set
                if (session.getSlaveNodeUuid() == null && session.getSlaveVolumeId() == null) {
                    // set it from the one in db
                    session.setSlaveNodeUuid(sessionInDb.getSlaveNodeUuid());
                    session.setSlaveVolumeId(sessionInDb.getSlaveVolumeId());
                }
                // if even the updated session has no slave ids, try setting it by querying db
                if (session.getSlaveNodeUuid() == null && session.getSlaveVolumeId() == null) {
                    updateSlaveNodeAndVolumeId(session);
                }
                session.setId(sessionInDb.getId());
                geoRepDao.updateSession(session);
            }
            updateSessionDetailsInDB(session);
            updateDiscoveredSessionConfig(cluster, session);
        }
    }

    protected void updateDiscoveredSessionConfig(Cluster cluster, GlusterGeoRepSession session) {
        List<GlusterGeoRepSessionConfiguration> sessionConfigList = getSessionConfigFromCLI(cluster, session);
        if (sessionConfigList == null) {
            log.info("No configuration information returned from VDS for session '{}'", session.getSessionKey());
            return;
        }
        List<GlusterGeoRepSessionConfiguration> existingSessionConfigs =
                geoRepDao.getGeoRepSessionConfig(session.getId());
        Map<String, GlusterGeoRepSessionConfiguration> existingKeyConfigMap =
                prepareMapOfGeoRepSessionConfigs(existingSessionConfigs);
        for (GlusterGeoRepSessionConfiguration sessionConfig : sessionConfigList) {
            //update sessionId for fetched object.
            sessionConfig.setId(session.getId());
            // check if session config not same as in db
            if (!existingSessionConfigs.contains(sessionConfig)) {
                // confirm that it exists in db, which means config has been updated
                if (existingKeyConfigMap.containsKey(sessionConfig.getKey())) {
                    geoRepDao.updateConfig(sessionConfig);
                    String oldValue = existingKeyConfigMap.get(sessionConfig.getKey()).getValue();
                    logGeoRepMessage(AuditLogType.GEOREP_OPTION_CHANGED_FROM_CLI,
                            cluster,
                            getOptionChangedCustomVars(session,
                                    sessionConfig.getKey(),
                                    sessionConfig.getValue(),
                                    oldValue));
                } else {
                    geoRepDao.saveConfig(sessionConfig);
                    logGeoRepMessage(AuditLogType.GEOREP_OPTION_SET_FROM_CLI,
                            cluster,
                            getOptionChangedCustomVars(session, sessionConfig.getKey(), sessionConfig.getValue(), null));
                }
            }
        }
        // If configs are reset some of them will disappear from config list
        Map<String, GlusterGeoRepSessionConfiguration> sessionKeyConfigMap =
                prepareMapOfGeoRepSessionConfigs(sessionConfigList);
        existingKeyConfigMap.keySet().removeAll(sessionKeyConfigMap.keySet());
        existingKeyConfigMap.keySet().forEach(key -> {
            GlusterGeoRepSessionConfiguration config = new GlusterGeoRepSessionConfiguration();
            config.setId(session.getId());
            config.setKey(key);
            geoRepDao.updateConfig(config);
        });
    }

    private Map<String, GlusterGeoRepSessionConfiguration> prepareMapOfGeoRepSessionConfigs(List<GlusterGeoRepSessionConfiguration> existingConfigs) {
        Map<String, GlusterGeoRepSessionConfiguration> keyConfigMap = new HashMap<>();
        if (existingConfigs != null) {
            for (GlusterGeoRepSessionConfiguration config : existingConfigs) {
                keyConfigMap.put(config.getKey(), config);
            }
        }
        return keyConfigMap;
    }

    private void updateSlaveNodeAndVolumeId(GlusterGeoRepSession session) {
        // populate ids from the ones that exist in engine
        List<VDS> slaveHosts = vdsDao.getAllForHostname(session.getSlaveHostName());
        if (!CollectionUtils.isEmpty(slaveHosts)) {
            session.setSlaveNodeUuid(slaveHosts.get(0).getId());
            GlusterVolumeEntity slaveVol =
                    volumeDao.getByName(slaveHosts.get(0).getClusterId(),
                            session.getSlaveVolumeName());
            if (slaveVol != null) {
                session.setSlaveVolumeId(slaveVol.getId());
            }
        }
    }

    private void updateSessionDetailsInDB(GlusterGeoRepSession session) {
        // update the session details object with session id.
        for (GlusterGeoRepSessionDetails sessDetails : session.getSessionDetails()) {
            sessDetails.setSessionId(session.getId());
        }
        geoRepDao.saveOrUpdateDetailsInBatch(session.getSessionDetails());
    }

    private void removeDeletedSessions(Cluster cluster,
                                       final Map<String, GlusterGeoRepSession> sessionsMap,
                                       GlusterVolumeEntity volume) {
        List<GlusterGeoRepSession> sessionsInDb;
        if (volume != null) {
            // syncing for a specific volume, so retrieve only that volume's sessions
            sessionsInDb = geoRepDao.getGeoRepSessions(volume.getId());
        } else {
            sessionsInDb = geoRepDao.getGeoRepSessionsInCluster(cluster.getId());
        }

        if (CollectionUtils.isEmpty(sessionsInDb)) {
            return;
        }
        List<GlusterGeoRepSession> sessionsToDelete = new ArrayList<>();
        for (GlusterGeoRepSession grepSession: sessionsInDb) {
            if (sessionsMap.get(grepSession.getSessionKey()) == null) {
                sessionsToDelete.add(grepSession);
            }
        }

        for (final GlusterGeoRepSession session : sessionsToDelete) {
            log.info("geo-rep session '{}' detected removed for volume '{}'",
                    session.getSessionKey(),
                    session.getMasterVolumeName());
            // check if geo-rep session is reference by a DR schedule
            List<StorageDomainDR> storageDRs = storageDomainDRDao.getWithGeoRepSession(session.getId());
            for (StorageDomainDR storageDR: storageDRs) {
                //delete and log deletion of storage DR - the schedule needs to be deleted as well
                log.info("Geo-rep session '{}'- for volume '{}' that has been deleted from CLI "
                        + "has associated DR sync schedules which will be removed",
                        session.getSessionKey(),
                        session.getMasterVolumeName());
                if (storageDR.getJobId() != null) {
                    scheduleDao.remove(storageDR.getJobId());
                }
                storageDomainDRDao.remove(storageDR.getStorageDomainId(), storageDR.getGeoRepSessionId());
                StorageDomainStatic storageDomain = storageDomainStaticDao.get(storageDR.getStorageDomainId());
                Map<String, String> customValues = new HashMap<>();
                customValues.put("storageDomainName", storageDomain.getName());
                customValues.put("geoRepSessionKey", session.getSessionKey());
                logGeoRepMessage(AuditLogType.STORAGE_DOMAIN_DR_DELETED, cluster, customValues);

            }
            geoRepDao.remove(session.getId());
            logGeoRepMessage(AuditLogType.GLUSTER_GEOREP_SESSION_DELETED_FROM_CLI, cluster, session);
        }
    }

    private void logGeoRepMessage(AuditLogType logType, Cluster cluster, final GlusterGeoRepSession session) {
        Map<String, String> customValues = new HashMap<>();
        customValues.put(GlusterConstants.VOLUME_NAME, session.getMasterVolumeName());
        customValues.put("geoRepSessionKey", session.getSessionKey());
        logUtil.logAuditMessage(cluster.getId(), cluster.getName(), null, null, logType, customValues);
    }

    private void logGeoRepMessage(AuditLogType logType, Cluster cluster, final Map<String, String> customVars) {
        logUtil.logAuditMessage(cluster.getId(), cluster.getName(), null, null, logType, customVars);
    }

    private Map<String, String> getOptionChangedCustomVars(final GlusterGeoRepSession session,
            String key,
            String value,
            String oldValue) {
        Map<String, String> keyValMap = new HashMap<>();
        keyValMap.put(GlusterConstants.VOLUME_NAME, session.getMasterVolumeName());
        keyValMap.put("geoRepSessionKey", session.getSessionKey());
        keyValMap.put("key", key);
        keyValMap.put("value", value);
        keyValMap.put("oldValue", oldValue);
        return keyValMap;
    }

    /**
     * This method updates the status depending on health of individual nodes
     */
    private void updateGeoRepStatus(GlusterVolumeEntity volume, GlusterGeoRepSession session) {
        List<Set<GeoRepSessionStatus>> list = new ArrayList<>();
        // grouped node status
        int replicaCount = volume.getReplicaCount() == 0 ? 1 : volume.getReplicaCount();
        if (replicaCount > volume.getBricks().size()) {
            session.setStatus(GeoRepSessionStatus.UNKNOWN);
            log.info("Not all bricks are available for this volume. replicaCount : {} , volumeBrickCount: {}",
                    replicaCount,
                    volume.getBricks().size());
            return;
        }
        for (int i = 0; i < volume.getBricks().size(); i = i + replicaCount) {
            Set<GeoRepSessionStatus> subVolumeStatusSet = new HashSet<>();
            int j = 0;
            while (j < replicaCount) {
                Guid brickId = volume.getBricks().get(i + j).getId();
                subVolumeStatusSet.add(getStatusForBrickFromSession(session, brickId));
                j++;
            }
            list.add(subVolumeStatusSet);
        }

        session.setStatus(GeoRepSessionStatus.ACTIVE);
        // iterate through grouped status to set consolidated status
        for (Set<GeoRepSessionStatus> subVolumeStatusValues : list) {
            if (subVolumeStatusValues.contains(GeoRepSessionStatus.ACTIVE)) {
                // healthy
                continue;
            } else if (subVolumeStatusValues.contains(GeoRepSessionStatus.FAULTY)) {
                session.setStatus(GeoRepSessionStatus.FAULTY);
                // if any one of the sub-volume is faulty, the overall session status if faulty
                return;
            }
            // override status in case of these values
            if (ArrayUtils.contains(overridableStatuses, session.getStatus())) {
                if (subVolumeStatusValues.size() == 1) {
                    session.setStatus((GeoRepSessionStatus) subVolumeStatusValues.toArray()[0]);
                } else {
                    // if status values in sub-volume are not the same, what do we do?
                    // this should not happen, so we'll log it for now
                    log.info("Multiple status values found in volume '{}'", session.getMasterVolumeName());
                }
            }
        }

    }

    private GeoRepSessionStatus getStatusForBrickFromSession(GlusterGeoRepSession session, Guid masterBrickId) {
        if (session.getSessionDetails() == null) {
            return null;
        }
        for (GlusterGeoRepSessionDetails sessionDetail : session.getSessionDetails()) {
            if (sessionDetail.getMasterBrickId().equals(masterBrickId)) {
                return sessionDetail.getStatus();
            }
        }
        return GeoRepSessionStatus.UNKNOWN;
    }

    private Map<String, GlusterGeoRepSession> getSessionsFromCLI(Cluster cluster, GlusterVolumeEntity volume) {
        VDS upServer = glusterUtil.getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.debug("No UP server found in cluster '{}' for geo-rep monitoring", cluster.getName());
            return null;
        }
        String volName = volume != null ? volume.getName() : null;
        // get details of geo-rep sessions in cluster
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeGeoRepSessionList,
                new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(), volName));
        if (returnValue.getSucceeded()) {
            List<GlusterGeoRepSession> sessions = (List<GlusterGeoRepSession>) returnValue.getReturnValue();
            Map<String, GlusterGeoRepSession> sessionsMap = new HashMap<>();
            if (sessions == null) {
                return sessionsMap;
            }
            for (GlusterGeoRepSession session : sessions) {
                sessionsMap.put(session.getSessionKey(), session);
            }
            return sessionsMap;
        } else {
            log.error("VDS error {}", returnValue.getVdsError().getMessage());
            log.debug("VDS error", returnValue.getVdsError());
            return null;
        }

    }

    private List<GlusterGeoRepSessionDetails> getSessionDetailFromCLI(Cluster cluster, GlusterGeoRepSession session) {
        VDS upServer = glusterUtil.getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.debug("No UP server found in cluster: {} for geo-rep monitoring", cluster.getName());
            return null;
        }
        try {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.GetGlusterVolumeGeoRepSessionStatus,
                            new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                    session.getMasterVolumeName(),
                                    session.getSlaveHostName(),
                                    session.getSlaveVolumeName(),
                                    session.getUserName()));
            if (returnValue.getSucceeded()) {
                return (List<GlusterGeoRepSessionDetails>) returnValue.getReturnValue();
            } else {
                log.error("VDS error {}", returnValue.getVdsError().getMessage());
                log.debug("VDS error", returnValue.getVdsError());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception getting geo-rep status from vds {}", e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private List<GlusterGeoRepSessionConfiguration> getSessionConfigFromCLI(Cluster cluster,
            GlusterGeoRepSession session) {
        VDS upServer = glusterUtil.getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.debug("No UP server found in cluster: {} for geo-rep monitoring", cluster.getName());
            return null;
        }
        try {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.GetGlusterVolumeGeoRepConfigList,
                            new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                    session.getMasterVolumeName(),
                                    session.getSlaveHostName(),
                                    session.getSlaveVolumeName(),
                                    session.getUserName()));
            if (returnValue.getSucceeded()) {
                return (List<GlusterGeoRepSessionConfiguration>) returnValue.getReturnValue();
            } else {
                log.error("VDS error {}", returnValue.getVdsError().getMessage());
                log.debug("VDS error", returnValue.getVdsError());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception getting geo-rep status from vds {}", e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private GlusterVolumeEntity getVolume(Cluster cluster, String masterVolumeName) {
        return volumeDao.getByName(cluster.getId(), masterVolumeName);
    }

    private boolean supportsGlusterGeoRepFeature(Cluster cluster) {
        return cluster.supportsGlusterService();
    }
}
