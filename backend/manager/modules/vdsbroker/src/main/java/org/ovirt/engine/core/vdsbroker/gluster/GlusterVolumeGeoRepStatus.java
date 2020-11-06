package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.gluster.GeoRepCrawlStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterVolumeGeoRepStatus extends StatusReturn {

    protected static final Logger log = LoggerFactory.getLogger(GlusterVolumeGeoRepStatus.class);

    protected static final String VOLUME_NAME = "volumeName";
    protected static final String BRICKS = "bricks";
    private static final String SESSIONS = "sessions";
    private static final String SESSION_KEY = "sessionKey";
    private static final String HOST_UUID = "hostUuid";
    private static final String BRICK_NAME = "brickName";
    private static final String REMOTE_HOST = "remoteHost";
    private static final String STATUS = "status";
    private static final String CHECK_POINT_STATUS = "checkpointStatus";
    private static final String CRAWL_STATUS = "crawlStatus";
    private static final String REMOTE_VOL_NAME = "remoteVolumeName";

    protected final List<GlusterGeoRepSession> geoRepSessions = new ArrayList<>();

    protected GlusterGeoRepSessionDetails getSessionDetails(Map<String, Object> innerMap) {
        GlusterGeoRepSessionDetails details = new GlusterGeoRepSessionDetails();
        Guid masterNodeGlusterId;
        if (innerMap.containsKey(HOST_UUID)) {
            masterNodeGlusterId = new Guid(innerMap.get(HOST_UUID).toString());
        } else {
            log.error("Master node uuid is not available");
            return null;
        }
        String masterBrickDir = innerMap.containsKey(BRICK_NAME) ? innerMap.get(BRICK_NAME).toString() : null;
        GlusterServer glusterServer = getDbUtils().getServerByUuid(masterNodeGlusterId);
        if (glusterServer != null) {
            GlusterBrickEntity brick =
                    getDbUtils().getGlusterBrickByServerUuidAndBrickDir(glusterServer.getId(), masterBrickDir);
            if (brick != null) {
                details.setMasterBrickId(brick.getId());
            }
        }
        if (details.getMasterBrickId() == null) {
            log.error("Brick information could not be retrieved for gluster host id {} and brick dir {}",
                    masterNodeGlusterId,
                    masterBrickDir);
        }

        String slave = innerMap.containsKey(REMOTE_HOST) && innerMap.get(REMOTE_HOST) != null
                ? innerMap.get(REMOTE_HOST).toString()
                : null;
        details.setSlaveHostName(slave);
        details.setStatus(GeoRepSessionStatus.from((String) innerMap.get(STATUS)));
        details.setCrawlStatus(GeoRepCrawlStatus.from((String) innerMap.get(CRAWL_STATUS)));
        details.setCheckPointStatus((String) innerMap.get(CHECK_POINT_STATUS));
        return details;
    }

    protected GlusterGeoRepSession getSession(String masterVolumeName, Map<String, Object> innerMap) {
        GlusterGeoRepSession geoRepSession = new GlusterGeoRepSession();
        // sessionKey in the form - the uuid is the gluster server uuid on master
        // <sessionKey>11ae7a03-e793-4270-8fc4-b42def8b3051:ssh://192.168.122.14::slave2:bd52ddf1-9659-4168-8197-c62e9f3e855c</sessionKey>
        String sessionKey = (String) innerMap.get(SESSION_KEY);
        String[] sessSplit = sessionKey.split("([://]+)");
        // Older gluster versions doesn't have slave volume ID in the sessionKey, it is added in Glusterfs 3.7.12
        String slaveNode = sessSplit[2];
        if(slaveNode.contains("@")) {
            String[] hostComponents = slaveNode.split("@");
            slaveNode = hostComponents[hostComponents.length - 1];
            geoRepSession.setUserName(hostComponents[0]);
        }
        String slaveVolume = (String) innerMap.get(REMOTE_VOL_NAME);
        geoRepSession.setSlaveHostName(slaveNode);
        geoRepSession.setSlaveVolumeName(slaveVolume);
        geoRepSession.setSessionKey(sessionKey);
        geoRepSession.setMasterVolumeName(masterVolumeName);
        return geoRepSession;
    }

    private GlusterDBUtils getDbUtils() {
        return Injector.get(GlusterDBUtils.class);
    }

    @SuppressWarnings("unchecked")
    protected void populateSessions(Map<String, Object> geoRepVolSessions) {
        for (Entry<String, Object> entry : geoRepVolSessions.entrySet()) {
            log.debug("received session information for volume '{}'", entry.getKey());
            String masterVolName = entry.getKey();

            Map<String, Object> sessionsMap = (Map<String, Object>) entry.getValue();

            if (sessionsMap.containsKey(SESSIONS)) {
                for (Object session : (Object[]) sessionsMap.get(SESSIONS)) {
                    geoRepSessions.add(populateSession(masterVolName, (Map<String, Object>) session));
                }
            }
        }

    }

    private GlusterGeoRepSession populateSession(String volumeName, Map<String, Object> innerMap) {
        GlusterGeoRepSession geoRepSession = getSession(volumeName, innerMap);
        ArrayList<GlusterGeoRepSessionDetails> geoRepSessionDetails = new ArrayList<>();
        if (innerMap.containsKey(BRICKS)) {
            for (Object brickSession : (Object[]) innerMap.get(BRICKS)) {
                geoRepSessionDetails.add(getSessionDetails((Map<String, Object>) brickSession));
            }
        }
        geoRepSession.setSessionDetails(geoRepSessionDetails);
        return geoRepSession;
    }

    public GlusterVolumeGeoRepStatus(Map<String, Object> innerMap) {
        this(innerMap, true);
    }

    public GlusterVolumeGeoRepStatus(Map<String, Object> innerMap, boolean includeSessions) {
        super(innerMap);
        if (includeSessions && innerMap.containsKey(SESSIONS)) {
            populateSessions((Map<String, Object>) innerMap.get(SESSIONS));
        }
    }

    public List<GlusterGeoRepSession> getGeoRepSessions() {
        return geoRepSessions;
    }

}
