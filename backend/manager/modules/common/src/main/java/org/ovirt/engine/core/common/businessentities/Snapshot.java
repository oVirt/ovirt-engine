package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * The snapshot represents a "frozen" point in time of a VM.<br>
 * <br>
 * The snapshot contains the VM's configuration (or an indicator if such configuration exists):
 * <ul>
 * <li>The VM's own configuration</li>
 * <li>The disks which were attached</li>
 * <li>The NIcs that were configured</li>
 * <li>Any additional devices which were relevant</li>
 * </ul>
 */
public class Snapshot extends IVdcQueryable implements BusinessEntityWithStatus<Guid, Snapshot.SnapshotStatus> {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 5883196978129104663L;

    /**
     * The snapshot ID uniquely identifies a snapshot in the system.
     */
    private Guid id;

    /**
     * The status of the snapshot (locked, ok, etc).
     */
    private SnapshotStatus status;

    /**
     * The ID of the VM this snapshot was taken for.
     */
    private Guid vmId;

    /**
     * The configuration of the VM at the snapshot time (might be <code>null</code> if no configuration was saved).
     */
    private String vmConfiguration;

    /**
     * Read-only (calculated field) to see if the VM configuration is available or not.<br>
     * <b>Note: </b>Only indicative in cases where VM configuration is <b>NOT</b> returned.
     */
    private boolean vmConfigurationAvailable;

    /**
     * The type of snapshot taken (regular, live, etC).
     */
    private SnapshotType type;

    /**
     * A short description which the user gave the snapshot.
     */
    private String description;

    /**
     * When was the snapshot taken.
     */
    private Date creationDate;

    /**
     * A list of the applications which were installed on the guest VM at the time of the snapshot (might be
     * <code>null</code>).
     */
    private String appList;

    /**
     * The volume that contains the memory state of the VM
     */
    private String memoryVolume;

    /**
     * Disk images of the snapshots
     */
    private List<DiskImage> diskImages;

    public Snapshot() {
        this(true);
    }

    public Snapshot(boolean vmConfigurationAvailable) {
        this.vmConfigurationAvailable = vmConfigurationAvailable;
        this.memoryVolume = "";
        this.diskImages = new ArrayList<DiskImage>();
    }

    public Snapshot(Guid id,
            SnapshotStatus status,
            Guid vmId,
            String vmConfiguration,
            SnapshotType type,
            String description,
            Date creationDate,
            String appList) {
        this();
        this.id = id;
        this.status = status;
        this.vmId = vmId;
        this.vmConfiguration = vmConfiguration;
        this.type = type;
        this.description = description;
        this.creationDate = creationDate;
        this.appList = appList;
    }

    public Snapshot(Guid id,
            SnapshotStatus status,
            Guid vmId,
            String vmConfiguration,
            SnapshotType type,
            String description,
            Date creationDate,
            String appList,
            String memoryVolume) {
        this(id, status, vmId, vmConfiguration, type, description, creationDate, appList);
        setMemoryVolume(memoryVolume);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public SnapshotStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(SnapshotStatus status) {
        this.status = status;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public String getVmConfiguration() {
        return vmConfiguration;
    }

    public void setVmConfiguration(String vmConfiguration) {
        this.vmConfiguration = vmConfiguration;
    }

    public boolean isVmConfigurationAvailable() {
        return vmConfigurationAvailable;
    }

    public SnapshotType getType() {
        return type;
    }

    public void setType(SnapshotType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAppList() {
        return appList;
    }

    public void setAppList(String appList) {
        this.appList = appList;
    }

    public String getMemoryVolume() {
        return memoryVolume;
    }

    public boolean containsMemory() {
        return !memoryVolume.isEmpty();
    }

    public void setMemoryVolume(String memoryVolume) {
        this.memoryVolume = memoryVolume == null ? "" : memoryVolume;
    }

    public List<DiskImage> getDiskImages() {
        return diskImages;
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        this.diskImages = diskImages;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((appList == null) ? 0 : appList.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((memoryVolume == null) ? 0 : memoryVolume.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((vmConfiguration == null) ? 0 : vmConfiguration.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((diskImages == null) ? 0 : diskImages.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Snapshot other = (Snapshot) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(appList, other.appList)
                && ObjectUtils.objectsEqual(creationDate, other.creationDate)
                && ObjectUtils.objectsEqual(description, other.description)
                && ObjectUtils.objectsEqual(memoryVolume, other.memoryVolume)
                && status == other.status
                && type == other.type
                && ObjectUtils.objectsEqual(vmConfiguration, other.vmConfiguration)
                && ObjectUtils.objectsEqual(vmId, other.vmId)
                && ObjectUtils.objectsEqual(diskImages, other.diskImages));
    }

    public enum SnapshotStatus {
        OK,
        LOCKED,
        IN_PREVIEW
    }

    public static enum SnapshotType {
        REGULAR("Regular"),
        ACTIVE("Active"),
        STATELESS("Stateless"),
        PREVIEW("In Preview"),
        NEXT_RUN("Next Run");

        private String typeName;

        private SnapshotType(String typeName) {
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }
    }
}
