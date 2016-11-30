package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusDetail;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeTaskReturn extends GlusterTaskInfoReturn {
    private static final String HOST_LIST = "hosts";
    private static final String SUMMARY = "summary";
    private static final String HOST_NAME = "name";
    private static final String HOST_UUID = "id";
    private static final String FILES_MOVED = "filesMoved";
    private static final String TOTAL_SIZE_MOVED = "totalSizeMoved";
    private static final String FILES_SCANNED = "filesScanned";
    private static final String FILES_FAILED = "filesFailed";
    private static final String FILES_SKIPPED = "filesSkipped";
    private static final String STATUS = "status";
    private static final String RUNTIME = "runtime";

    private final GlusterVolumeTaskStatusEntity statusDetails = new GlusterVolumeTaskStatusEntity();

    @SuppressWarnings("unchecked")
    public GlusterVolumeTaskReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (innerMap.containsKey(HOST_LIST)) {
            for (Object nodeStatus : (Object[]) innerMap.get(HOST_LIST)) {
                statusDetails.getHostwiseStatusDetails().add(getStatusForNode((Map<String, Object>) nodeStatus));
            }
        }
        if (innerMap.containsKey(SUMMARY)) {
            populateGlusterVolumeTaskStatusDetail(statusDetails.getStatusSummary(),
                    (Map<String, Object>) innerMap.get(SUMMARY));
        }
    }

    private GlusterVolumeTaskStatusForHost getStatusForNode(Map<String, Object> nodeStatus) {
        GlusterVolumeTaskStatusForHost rebalanceStatusForHost = new GlusterVolumeTaskStatusForHost();
        rebalanceStatusForHost.setHostName(nodeStatus.containsKey(HOST_NAME) ? (String) nodeStatus.get(HOST_NAME)
                : null);
        rebalanceStatusForHost.setHostUuid(nodeStatus.containsKey(HOST_UUID) ? new Guid((String) nodeStatus.get(HOST_UUID))
                : null);
        populateGlusterVolumeTaskStatusDetail(rebalanceStatusForHost, nodeStatus);

        return rebalanceStatusForHost;
    }

    private void populateGlusterVolumeTaskStatusDetail(GlusterVolumeTaskStatusDetail detail, Map<String, Object> map) {
        detail.setFilesScanned(map.containsKey(FILES_SCANNED) ? Long.parseLong(map.get(FILES_SCANNED).toString()) : 0);
        detail.setFilesMoved(map.containsKey(FILES_MOVED) ? Long.parseLong(map.get(FILES_MOVED).toString()) : 0);
        detail.setFilesFailed(map.containsKey(FILES_FAILED) ? Long.parseLong(map.get(FILES_FAILED).toString()) : 0);
        detail.setFilesSkipped(map.containsKey(FILES_SKIPPED) ? Long.parseLong(map.get(FILES_SKIPPED).toString()) : 0);
        detail.setTotalSizeMoved(map.containsKey(TOTAL_SIZE_MOVED) ? Long.parseLong(map.get(TOTAL_SIZE_MOVED).toString())
                : 0);
        detail.setStatus(map.containsKey(STATUS) ? GlusterAsyncTaskStatus.from(map.get(STATUS).toString())
                .getJobExecutionStatus() : JobExecutionStatus.UNKNOWN);
        detail.setRunTime(map.containsKey(RUNTIME) ? Double.parseDouble(map.get(RUNTIME).toString()) : 0);
    }

    public GlusterVolumeTaskStatusEntity getStatusDetails() {
        return statusDetails;
    }
}
