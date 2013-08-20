package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusDetail;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;

public class GlusterVolumeTaskReturnForXmlRpc extends GlusterTaskInfoReturnForXmlRpc {
    private static final String HOST_LIST = "hosts";
    private static final String SUMMARY = "summary";
    private static final String HOST_NAME = "name";
    private static final String FILES_MOVED = "filesMoved";
    private static final String TOTAL_SIZE_MOVED = "totalSizeMoved";
    private static final String FILES_SCANNED = "filesScanned";
    private static final String FILES_FAILED = "filesFailed";
    private static final String STATUS = "status";

    private final GlusterVolumeTaskStatusEntity statusDetails = new GlusterVolumeTaskStatusEntity();

    @SuppressWarnings("unchecked")
    public GlusterVolumeTaskReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (innerMap.containsKey(HOST_LIST)) {
            for (Object nodeStatus : (Object[])innerMap.get(HOST_LIST)) {
                statusDetails.getHostwiseStatusDetails().add(getStatusForNode((Map<String, Object>)nodeStatus));
            }
        }

        if (innerMap.containsKey(SUMMARY)) {
            populateGlusterVolumeTaskStatusDetail(statusDetails.getStatusSummary(), (Map<String, Object>)innerMap.get(SUMMARY));
        }
    }

    private GlusterVolumeTaskStatusForHost getStatusForNode(Map<String, Object> nodeStatus) {
        GlusterVolumeTaskStatusForHost rebalanceStatusForHost = new GlusterVolumeTaskStatusForHost();
        rebalanceStatusForHost.setHostName((String)nodeStatus.get(HOST_NAME));
        populateGlusterVolumeTaskStatusDetail(rebalanceStatusForHost, nodeStatus);

        return rebalanceStatusForHost;
    }

    private void populateGlusterVolumeTaskStatusDetail(GlusterVolumeTaskStatusDetail detail, Map<String, Object> map) {
        detail.setFilesScanned((Integer)map.get(FILES_SCANNED));
        detail.setFilesMoved((Integer)map.get(FILES_MOVED));
        detail.setFilesFailed((Integer)map.get(FILES_FAILED));
        detail.setTotalSizeMoved(((Integer)map.get(TOTAL_SIZE_MOVED)).longValue());
        detail.setStatus(GlusterAsyncTaskStatus.from((String)map.get(STATUS)).getJobExecutionStatus());
    }

    public GlusterVolumeTaskStatusEntity getStatusDetails() {
        return statusDetails;
    }
}
