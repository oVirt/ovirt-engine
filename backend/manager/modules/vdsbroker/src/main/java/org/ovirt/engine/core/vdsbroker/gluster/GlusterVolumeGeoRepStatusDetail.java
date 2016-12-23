package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;

public class GlusterVolumeGeoRepStatusDetail extends GlusterVolumeGeoRepStatus {

    private static final String SESSION_STATUS = "sessionStatus";
    private static final String LAST_SYNCED = "lastSynced";
    private static final String FAILURES = "failures";
    private static final String DATA = "data";
    private static final String META = "meta";
    private static final String ENTRY = "entry";
    private static final String TIMEZONE = "timeZone";
    private static final String CHECK_POINT_COMPLETION_TIME = "checkpointCompletionTime";
    private static final String CHECK_POINT_COMPLETED = "checkpointCompleted";
    private static final String CHECK_POINT_TIME = "checkpointTime";

    private final ArrayList<GlusterGeoRepSessionDetails> geoRepDetails = new ArrayList<>();

    private Long parseSafeLong(Map<String, Object> innerMap, String key) {
        return innerMap.containsKey(key) && StringUtils.isNumeric(innerMap.get(key).toString()) ? Long.parseLong(innerMap.get(key).toString())
                : null;
    }

    @Override
    protected GlusterGeoRepSessionDetails getSessionDetails(Map<String, Object> innerMap) {
        GlusterGeoRepSessionDetails details = super.getSessionDetails(innerMap);
        if (details != null) {
            Long dataOpsPending = parseSafeLong(innerMap, DATA);
            Long metaOpsPending = parseSafeLong(innerMap, META);
            Long entryOpsPending = parseSafeLong(innerMap, ENTRY);
            Long failures = parseSafeLong(innerMap, FAILURES);
            details.setDataOpsPending(dataOpsPending);
            details.setMetaOpsPending(metaOpsPending);
            details.setEntryOpsPending(entryOpsPending);
            details.setFailures(failures);

            if (innerMap.containsKey(CHECK_POINT_COMPLETED)) {
                if ("YES".equalsIgnoreCase(innerMap.get(CHECK_POINT_COMPLETED).toString())) {
                    details.setCheckpointCompleted(true);
                } else {
                    details.setCheckpointCompleted(false);
                }
            }
            String timezone = innerMap.containsKey(TIMEZONE) ? innerMap.get(TIMEZONE).toString() : null;

            Long lastSynced = parseSafeLong(innerMap, LAST_SYNCED);
            Long checkPointTime = parseSafeLong(innerMap, CHECK_POINT_TIME);
            Long checkPointCompletionTime = parseSafeLong(innerMap, CHECK_POINT_COMPLETION_TIME);
            details.setLastSyncedAt(lastSynced != null ? new Date(lastSynced * 1000L) : null);
            details.setCheckPointCompletedAt(checkPointCompletionTime != null ?
                    new Date(checkPointCompletionTime * 1000L) : null);
            details.setCheckPointTime(checkPointTime != null ? new Date(checkPointTime * 1000L) : null);
        }
        geoRepDetails.add(details);
        return details;
    }

    public GlusterVolumeGeoRepStatusDetail(Map<String, Object> innerMap) {
        super(innerMap, false);
        if (innerMap.containsKey(SESSION_STATUS)) {
            populateSessions((Map<String, Object>) innerMap.get(SESSION_STATUS));
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
