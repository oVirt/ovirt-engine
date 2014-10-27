package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;

public class GlusterVolumeGeoRepStatusDetailForXmlRpc extends GlusterVolumeGeoRepStatusForXmlRpc {

    private static final String FILES_SYNCED = "filesSyncd";
    private static final String FILES_PENDING = "filesPending";
    private static final String BYTES_PENDING = "bytesPending";
    private static final String DELETES_PENDING = "deletesPending";
    private static final String FILES_SKIPPED = "filesSkipped";

    private final ArrayList<GlusterGeoRepSessionDetails> geoRepDetails = new ArrayList<GlusterGeoRepSessionDetails>();
    private final List<GlusterGeoRepSession> geoRepSessions = new ArrayList<GlusterGeoRepSession>();

    private GlusterGeoRepSessionDetails populatePairDetails(Map<String, Object> innerMap) {
        GlusterGeoRepSessionDetails details = getSessionDetails(innerMap);
        if (details != null) {
            Long filesSynced =
                    innerMap.containsKey(FILES_SYNCED) ? Long.parseLong(innerMap.get(FILES_SYNCED).toString()) : null;
            Long filesPending =
                    innerMap.containsKey(FILES_PENDING) ? Long.parseLong(innerMap.get(FILES_PENDING).toString()) : null;
            Long bytesPending =
                    innerMap.containsKey(BYTES_PENDING) ? Long.parseLong(innerMap.get(BYTES_PENDING).toString()) : null;
            Long deletesPending =
                    innerMap.containsKey(DELETES_PENDING) ? Long.parseLong(innerMap.get(DELETES_PENDING).toString()) : null;
            Long filesSkipped =
                    innerMap.containsKey(FILES_SKIPPED) ? Long.parseLong(innerMap.get(FILES_SKIPPED).toString()) : null;

            details.setFilesPending(filesPending);
            details.setFilesSkipped(filesSkipped);
            details.setFilesSynced(filesSynced);
            details.setBytesPending(bytesPending);
            details.setDeletesPending(deletesPending);
        }
        return details;
    }

    @SuppressWarnings("unchecked")
    private void populateSessionDetails(Map<String, Object> innerMap) {
        if (innerMap.containsKey(VOLUME_NAME)) {
            String masterVolumeName = (String) innerMap.get(VOLUME_NAME);
            GlusterGeoRepSession session = getSession(masterVolumeName, innerMap);

            if (innerMap.containsKey(GEO_REP_PAIRS)) {
                for (Object sessionPair : (Object[]) innerMap.get(GEO_REP_PAIRS)) {
                    geoRepDetails.add(populatePairDetails((Map<String, Object>) sessionPair));
                }
            }
            session.setSessionDetails(geoRepDetails);
            geoRepSessions.add(session);
        }
    }

    public GlusterVolumeGeoRepStatusDetailForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap, false);
        if (innerMap.containsKey(GEO_REP)) {
            populateSessionDetails((Map<String, Object>) innerMap.get(GEO_REP));
        }
    }

    public List<GlusterGeoRepSessionDetails> getGeoRepDetails() {
        return geoRepDetails;
    }

    @Override
    public List<GlusterGeoRepSession> getGeoRepSessions() {
        return geoRepSessions;
    }
}
