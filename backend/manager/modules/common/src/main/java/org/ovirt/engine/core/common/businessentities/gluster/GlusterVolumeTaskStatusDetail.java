package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class GlusterVolumeTaskStatusDetail implements Serializable {
    private static final long serialVersionUID = -1134758927239004416L;

    private int filesMoved;
    private long totalSizeMoved;
    private int filesScanned;
    private int filesFailed;
    private int filesSkipped;
    private int runTime;
    private JobExecutionStatus status;

    public GlusterVolumeTaskStatusDetail() {
    }

    public int getFilesMoved() {
        return filesMoved;
    }
    public void setFilesMoved(int filesMoved) {
        this.filesMoved = filesMoved;
    }
    public long getTotalSizeMoved() {
        return totalSizeMoved;
    }
    public void setTotalSizeMoved(long size) {
        this.totalSizeMoved = size;
    }
    public int getFilesScanned() {
        return filesScanned;
    }
    public void setFilesScanned(int totalScannedCount) {
        this.filesScanned = totalScannedCount;
    }
    public int getFilesFailed() {
        return filesFailed;
    }
    public void setFilesFailed(int failuresCount) {
        this.filesFailed = failuresCount;
    }
    public int getFilesSkipped() {
        return filesSkipped;
    }
    public void setFilesSkipped(int filesSkipped) {
        this.filesSkipped = filesSkipped;
    }
    public int getRunTime() {
        return runTime;
    }
    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }
    public JobExecutionStatus getStatus() {
        return status;
    }
    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }
}
