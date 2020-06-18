package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
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
public class Snapshot implements Queryable, BusinessEntityWithStatus<Guid, Snapshot.SnapshotStatus> {

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

    private boolean vmConfigurationBroken;

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

    /** The ID of the disk that contains the memory dump */
    private Guid memoryDiskId;

    /** The ID of the disk that contains the VM metadata */
    private Guid metadataDiskId;

    /**
     * Disk images of the snapshots
     */
    private List<DiskImage> diskImages;

    /**
     * Changed fields between current and next run configuration
     * (applicable only for NEXT_RUN snapshot type)
     */
    private Set<String> changedFields;

    public Snapshot() {
        this(true);
    }

    public Snapshot(boolean vmConfigurationAvailable) {
        this.vmConfigurationAvailable = vmConfigurationAvailable;
        this.memoryVolume = "";
        this.diskImages = new ArrayList<>();
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
            Guid memoryDiskId,
            Guid metadataDiskId,
            Set<String> changedFields) {
        this(id, status, vmId, vmConfiguration, type, description, creationDate, appList);
        setMemoryDiskId(memoryDiskId);
        setMetadataDiskId(metadataDiskId);
        setChangedFields(changedFields);
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

    public boolean isVmConfigurationBroken() {
        return vmConfigurationBroken;
    }

    public void setVmConfigurationBroken(boolean vmConfigurationBroken) {
        this.vmConfigurationBroken = vmConfigurationBroken;
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

    public boolean containsMemory() {
        return memoryDiskId != null || metadataDiskId != null;
    }

    public Guid getMemoryDiskId() {
        return memoryDiskId;
    }

    public void setMemoryDiskId(Guid memoryDiskId) {
        this.memoryDiskId = memoryDiskId;
    }

    public Guid getMetadataDiskId() {
        return metadataDiskId;
    }

    public void setMetadataDiskId(Guid metadataDiskId) {
        this.metadataDiskId = metadataDiskId;
    }

    public List<DiskImage> getDiskImages() {
        return diskImages;
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        this.diskImages = diskImages;
    }

    public Set<String> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(Set<String> changedFields) {
        this.changedFields = changedFields;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                appList,
                creationDate,
                description,
                memoryVolume,
                status,
                type,
                vmConfiguration,
                vmId,
                diskImages,
                vmConfigurationBroken,
                changedFields
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Snapshot)) {
            return false;
        }
        Snapshot other = (Snapshot) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(appList, other.appList)
                && Objects.equals(creationDate, other.creationDate)
                && Objects.equals(description, other.description)
                && Objects.equals(memoryVolume, other.memoryVolume)
                && status == other.status
                && type == other.type
                && Objects.equals(vmConfiguration, other.vmConfiguration)
                && Objects.equals(vmId, other.vmId)
                && Objects.equals(diskImages, other.diskImages)
                && vmConfigurationBroken == other.vmConfigurationBroken
                && Objects.equals(changedFields, other.changedFields);
    }

    public enum SnapshotStatus {
        OK,
        LOCKED,
        IN_PREVIEW
    }

    public enum SnapshotType {
        REGULAR,
        ACTIVE,
        STATELESS,
        PREVIEW,
        NEXT_RUN
    }
}
