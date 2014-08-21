package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GeoRepCrawlStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class GlusterVolumeGeoRepStatusForXmlRpc extends StatusReturnForXmlRpc {
    protected static final String GEO_REP = "geo-rep";
    protected static final String VOLUME_NAME = "volumeName";
    protected static final String GEO_REP_PAIRS = "pairs";
    private static final String SESSIONS = "sessions";
    private static final String SESSION_SLAVE = "sessionSlave";
    private static final String MASTER_NODE_UUID = "masterNodeUuid";
    private static final String MASTER_BRICK = "masterBrick";
    private static final String SLAVE = "slave";
    private static final String STATUS = "status";
    private static final String CHECK_POINT_STATUS = "checkpointStatus";
    private static final String CRAWL_STATUS = "crawlStatus";

    private final List<GlusterGeoRepSession> geoRepSessions = new ArrayList<GlusterGeoRepSession>();

    private static final Log log = LogFactory.getLog(GlusterVolumesListReturnForXmlRpc.class);

    protected GlusterGeoRepSessionDetails getSessionDetails(Map<String, Object> innerMap, GlusterGeoRepSession session) {
        GlusterGeoRepSessionDetails details = new GlusterGeoRepSessionDetails();
        details.setSessionId(session.getId());
        Guid masterNodeGlusterId;
        if (innerMap.containsKey(MASTER_NODE_UUID)) {
            masterNodeGlusterId = new Guid(innerMap.get(MASTER_NODE_UUID).toString());
        } else {
            log.error("Master node uuid is not available");
            return null;
        }
        String masterBrickDir = (innerMap.containsKey(MASTER_BRICK)) ? innerMap.get(MASTER_BRICK).toString() : null;
        GlusterServer glusterServer = getDbUtils().getServerByUuid(masterNodeGlusterId);
        if (glusterServer != null) {
            GlusterBrickEntity brick =
                    getDbUtils().getGlusterBrickByServerUuidAndBrickDir(glusterServer.getId(), masterBrickDir);
            if (brick != null) {
                details.setMasterBrickId(brick.getId());
            }
        }
        if (details.getMasterBrickId() == null) {
            log.errorFormat("Brick information could not be retrieved for gluster host id %1 and brick dir %2",
                    masterNodeGlusterId,
                    masterBrickDir);
        }

        String slave = innerMap.containsKey(SLAVE) ? innerMap.get(SLAVE).toString() : null;
        String[] slaveSplit = (slave != null) ? slave.split("([://]+)") : null;
        if (slaveSplit != null && slaveSplit.length >= 2) {
            details.setSlaveHostName(slaveSplit[slaveSplit.length - 2]);
        }
        details.setStatus(GeoRepSessionStatus.from((String) innerMap.get(STATUS)));
        details.setCrawlStatus(GeoRepCrawlStatus.from((String) innerMap.get(CRAWL_STATUS)));
        details.setCheckPointStatus((String) innerMap.get(CHECK_POINT_STATUS));
        return details;
    }

    protected GlusterGeoRepSession getSession(String masterVolumeName, Map<String, Object> innerMap) {
        GlusterGeoRepSession geoRepSession = new GlusterGeoRepSession();
        // sessionslave in the form -
        // <session_slave>11ae7a03-e793-4270-8fc4-b42def8b3051:ssh://192.168.122.14::slave2</session_slave>
        String sessionKey = (String) innerMap.get(SESSION_SLAVE);
        String sessSplit[] = sessionKey.split("([://]+)");
        String sessionId = sessSplit[0];
        String slaveNode = sessSplit[sessSplit.length - 2];
        String slaveVolume = sessSplit[sessSplit.length - 1];
        geoRepSession.setId(Guid.createGuidFromString(sessionId));
        geoRepSession.setSlaveHostName(slaveNode);
        geoRepSession.setSlaveVolumeName(slaveVolume);
        geoRepSession.setSessionKey(sessionKey);
        geoRepSession.setMasterVolumeName(masterVolumeName);
        return geoRepSession;
    }

    private GlusterDBUtils getDbUtils() {
        return GlusterDBUtils.getInstance();
    }

    @SuppressWarnings("unchecked")
    private void populateSessions(Object[] geoRepVolSessions) {
        for (Object geoRepVolSession : geoRepVolSessions) {
            Map<String, Object> innerMap = (Map<String, Object>) geoRepVolSession;
            if (innerMap.containsKey(VOLUME_NAME)) {
                String masterVolName = (String) innerMap.get(VOLUME_NAME);
                if (innerMap.containsKey(SESSIONS)) {
                    for (Object session : (Object[]) innerMap.get(SESSIONS)) {
                        geoRepSessions.add(populateSession(masterVolName, (Map<String, Object>) session));
                    }
                }
            }
        }
    }

    private GlusterGeoRepSession populateSession(String volumeName, Map<String, Object> innerMap) {
        GlusterGeoRepSession geoRepSession = getSession(volumeName, innerMap);
        ArrayList<GlusterGeoRepSessionDetails> geoRepSessionDetails = new ArrayList<GlusterGeoRepSessionDetails>();
        if (innerMap.containsKey(GEO_REP_PAIRS)) {
            for (Object sessionPair : (Object[]) innerMap.get(GEO_REP_PAIRS)) {
                geoRepSessionDetails.add(getSessionDetails((Map<String, Object>) sessionPair, geoRepSession));
            }
        }
        geoRepSession.setSessionDetails(geoRepSessionDetails);
        return geoRepSession;
    }

    public GlusterVolumeGeoRepStatusForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(GEO_REP)) {
            populateSessions((Object[]) innerMap.get(GEO_REP));
        }
    }

    public List<GlusterGeoRepSession> getGeoRepSessions() {
        return geoRepSessions;
    }

}
