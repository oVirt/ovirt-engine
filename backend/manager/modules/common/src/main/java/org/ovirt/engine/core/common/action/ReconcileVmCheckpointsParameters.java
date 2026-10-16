package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameters for ReconcileVmCheckpoints: the bitmap names that were found unusable on the
 * disks (broken checkpoint bitmaps and orphans), with a cursor for the serial execution of
 * the bitmap removal children.
 */
public class ReconcileVmCheckpointsParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 1745091122110269217L;

    private List<VmCheckpoint> reconcilingCheckpoints = new ArrayList<>();
    private List<String> orphanBitmaps = new ArrayList<>();
    private int completedBitmapsCount;

    public ReconcileVmCheckpointsParameters() {
    }

    public ReconcileVmCheckpointsParameters(Guid vmId) {
        super(vmId);
    }

    public List<VmCheckpoint> getReconcilingCheckpoints() {
        return reconcilingCheckpoints;
    }

    public void setReconcilingCheckpoints(List<VmCheckpoint> reconcilingCheckpoints) {
        this.reconcilingCheckpoints = reconcilingCheckpoints;
    }

    public List<String> getOrphanBitmaps() {
        return orphanBitmaps;
    }

    public void setOrphanBitmaps(List<String> orphanBitmaps) {
        this.orphanBitmaps = orphanBitmaps;
    }

    /**
     * @return the total number of bitmaps to remove - one per broken checkpoint plus one per
     *         orphan
     */
    public int getBitmapsCount() {
        return reconcilingCheckpoints.size() + orphanBitmaps.size();
    }

    public int getCompletedBitmapsCount() {
        return completedBitmapsCount;
    }

    public void advanceToNextBitmap() {
        completedBitmapsCount++;
    }
}
