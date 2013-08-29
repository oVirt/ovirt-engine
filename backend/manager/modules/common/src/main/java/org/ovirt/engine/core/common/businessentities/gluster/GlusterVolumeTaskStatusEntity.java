package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class GlusterVolumeTaskStatusEntity implements Serializable {
    private static final long serialVersionUID = -1134758927239004414L;

    private Date startTime;
    private Date statusTime;
    private List<GlusterVolumeTaskStatusForHost> hostwiseStatusDetails;
    private GlusterVolumeTaskStatusDetail statusSummary;

    public GlusterVolumeTaskStatusEntity() {
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public List<GlusterVolumeTaskStatusForHost> getHostwiseStatusDetails() {
        return hostwiseStatusDetails;
    }

    public void setHostwiseStatusDetails(List<GlusterVolumeTaskStatusForHost> hostStatusDetails) {
        this.hostwiseStatusDetails = hostStatusDetails;
    }

    public GlusterVolumeTaskStatusDetail getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(GlusterVolumeTaskStatusDetail summary) {
        this.statusSummary = summary;
    }
}
