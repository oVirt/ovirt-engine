package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class GlusterVolumeTaskStatusDetail implements Serializable {
    private static final long serialVersionUID = -1134758927239004416L;

    private long filesMoved;
    private long totalSizeMoved;
    private long filesScanned;
    private long filesFailed;
    private long filesSkipped;
    private double runTime;
    private JobExecutionStatus status;

    public GlusterVolumeTaskStatusDetail() {
    }

    public long getFilesMoved() {
        return filesMoved;
    }
    public void setFilesMoved(long filesMoved) {
        this.filesMoved = filesMoved;
    }
    public long getTotalSizeMoved() {
        return totalSizeMoved;
    }
    public void setTotalSizeMoved(long size) {
        this.totalSizeMoved = size;
    }
    public long getFilesScanned() {
        return filesScanned;
    }
    public void setFilesScanned(long totalScannedCount) {
        this.filesScanned = totalScannedCount;
    }
    public long getFilesFailed() {
        return filesFailed;
    }
    public void setFilesFailed(long failuresCount) {
        this.filesFailed = failuresCount;
    }
    public long getFilesSkipped() {
        return filesSkipped;
    }
    public void setFilesSkipped(long filesSkipped) {
        this.filesSkipped = filesSkipped;
    }
    public double getRunTime() {
        return runTime;
    }
    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }
    public JobExecutionStatus getStatus() {
        return status;
    }
    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }
}
