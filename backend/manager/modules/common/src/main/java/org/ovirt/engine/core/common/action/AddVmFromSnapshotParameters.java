package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for AddVmFromSnapshot command
 */
public class AddVmFromSnapshotParameters extends VmManagementParametersBase implements Serializable {
    private static final long serialVersionUID = -3400982291165788716L;

    /**
     * Holds information of change DiskImages by client.
     * For images that are not stated in this collection,
     * information from the ancestor image will be used for cloning.
     * The information that is relevant is the volume information
     */
    private ArrayList<DiskImage> diskInfoList = new ArrayList<DiskImage>();

    public ArrayList<DiskImage> getDiskInfoList() {
        return diskInfoList;
    }

    public void setDiskInfoList(ArrayList<DiskImage> diskInfoList) {
        this.diskInfoList = diskInfoList;
    }

    //Unique Identifier of Source Snapshot
    private Guid sourceSnapshotId;

    public Guid getSourceSnapshotId() {
        return sourceSnapshotId;
    }

    public void setSourceSnapshotId(Guid sourceSnapshotId) {
        this.sourceSnapshotId = sourceSnapshotId;
    }

    public AddVmFromSnapshotParameters() {
        setDontCheckTemplateImages(true);
    }

    public AddVmFromSnapshotParameters(VmStatic vmStatic, ArrayList<DiskImage> diskInfoList, Guid sourceSnapshotId) {
        super(vmStatic);
        setVmId(Guid.Empty);
        this.sourceSnapshotId = sourceSnapshotId;
        setDiskInfoList(diskInfoList);
        setDontCheckTemplateImages(true);
    }

}
