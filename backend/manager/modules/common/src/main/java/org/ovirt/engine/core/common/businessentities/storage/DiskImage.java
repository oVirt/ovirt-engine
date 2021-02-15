package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

public class DiskImage extends DiskImageBase {

    private static final long serialVersionUID = 3185087852755356847L;

    private Date lastModifiedDate;
    private Date snapshotCreationDate;
    private List<String> storagesNames;
    private long actualSizeInBytes;
    private long apparentSizeInBytes;
    private Long initialSizeInBytes;
    private int readRateFromDiskImageDynamic;
    private long readOpsFromDiskImageDynamic;
    private int writeRateFromDiskImageDynamic;
    private long writeOpsFromDiskImageDynamic;

    // Latency fields from DiskImageDynamic which are measured in seconds.
    private Double readLatency;
    private Double writeLatency;
    private Double flushLatency;

    private String appList;
    // TODO from storage_domain_static
    private Guid storagePoolId;
    private List<DiskImage> snapshots;
    private double actualDiskWithSnapthotsSize;
    private List<Guid> quotaIds;
    private List<String> quotaNames;
    private List<Guid> diskProfileIds;
    private List<String> diskProfileNames;
    private String vmSnapshotDescription;

    /** when this object represents a disk that resides within an OVA,
     *  this field contains the path of the volume within the OVA */
    private String remotePath;

    public DiskImage() {
        setParentId(Guid.Empty);
        setCreationDate(new Date());
        setLastModifiedDate(getCreationDate());
        snapshots = new ArrayList<>();
    }

    public Guid getImageId() {
        return getImage().getId();
    }

    public void setImageId(Guid id) {
        getImage().setId(id);
    }

    private VmEntityType vmEntityType;

    @Override
    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    @Override
    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    public Boolean getActive() {
        return getImage().isActive();
    }

    @Override
    @JsonIgnore
    public boolean isDiskSnapshot() {
        return !getActive();
    }

    public void setDiskSnapshot(boolean diskSnapshot) {
        setActive(!diskSnapshot);
    }

    @JsonIgnore
    public Guid getSnapshotId() {
        return isDiskSnapshot() ? getVmSnapshotId() : null;
    }

    public void setActive(boolean active) {
        getImage().setActive(active);
    }

    public VolumeClassification getVolumeClassification() {
        return getImage().getVolumeClassification();
    }

    public void setVolumeClassification(VolumeClassification volumeClassification) {
        getImage().setVolumeClassification(volumeClassification);
    }

    public Date getCreationDate() {
        return getImage().getCreationDate();
    }

    public void setCreationDate(Date creationDate) {
        getImage().setCreationDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getSnapshotCreationDate() {
        return snapshotCreationDate;
    }

    public void setSnapshotCreationDate(Date snapshotCreationDate) {
        this.snapshotCreationDate = snapshotCreationDate;
    }

    public long getActualSizeInBytes() {
        return actualSizeInBytes;
    }

    public void setActualSizeInBytes(long size) {
        actualSizeInBytes = size;
        setActualSize(getActualSizeInBytes() * 1.0 / (1024 * 1024 * 1024));
    }

    public Long getInitialSizeInBytes() {
        return initialSizeInBytes;
    }

    public void setInitialSizeInBytes(Long size) {
        initialSizeInBytes = size;
    }

    public boolean hasActualSize() {
        return true;
    }

    public long getApparentSizeInBytes() {
        return apparentSizeInBytes;
    }

    public void setApparentSizeInBytes(long apparentSizeInBytes) {
        this.apparentSizeInBytes = apparentSizeInBytes;
    }

    public int getReadRate() {
        return readRateFromDiskImageDynamic;
    }

    public void setReadRate(int readRateFromDiskImageDynamic) {
        this.readRateFromDiskImageDynamic = readRateFromDiskImageDynamic;
    }

    public long getReadOps() {
        return readOpsFromDiskImageDynamic;
    }

    public void setReadOps(long readOpsFromDiskImageDynamic) {
        this.readOpsFromDiskImageDynamic = readOpsFromDiskImageDynamic;
    }

    public int getWriteRate() {
        return writeRateFromDiskImageDynamic;
    }

    public void setWriteRate(int writeRateFromDiskImageDynamic) {
        this.writeRateFromDiskImageDynamic = writeRateFromDiskImageDynamic;
    }

    public long getWriteOps() {
        return writeOpsFromDiskImageDynamic;
    }

    public void setWriteOps(long writeOpsFromDiskImageDynamic) {
        this.writeOpsFromDiskImageDynamic = writeOpsFromDiskImageDynamic;
    }

    public Double getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(Double readLatency) {
        this.readLatency = readLatency;
    }

    public Double getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(Double writeLatency) {
        this.writeLatency = writeLatency;
    }

    public Double getFlushLatency() {
        return flushLatency;
    }

    public void setFlushLatency(Double flushLatency) {
        this.flushLatency = flushLatency;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppList() {
        return appList;
    }

    public void setAppList(String appList) {
        this.appList = appList;
    }

    public Guid getImageTemplateId() {
        return getImage().getTemplateImageId();
    }

    public void setImageTemplateId(Guid guid) {
        getImage().setTemplateImageId(guid);
    }

    public Guid getParentId() {
        return getImage().getParentId();
    }

    public void setParentId(Guid parentId) {
        getImage().setParentId(parentId);
    }

    public ImageStatus getImageStatus() {
        return getImage().getStatus();
    }

    public void setImageStatus(ImageStatus imageStatus) {
        getImage().setStatus(imageStatus);
    }

    public Date getLastModified() {
        return getImage().getLastModified();
    }

    public void setLastModified(Date lastModified) {
        getImage().setLastModified(lastModified);
    }

    private List<Guid> storageIds;
    private List<StorageType> storageTypes;

    public List<Guid> getStorageIds() {
        return storageIds;
    }

    public void setStorageIds(List<Guid> storageIds) {
        this.storageIds = storageIds;
    }

    public List<StorageType> getStorageTypes() {
        return storageTypes;
    }

    public void setStorageTypes(List<StorageType> storageTypes) {
        this.storageTypes = storageTypes;
    }

    public Guid getVmSnapshotId() {
        return getImage().getSnapshotId();
    }

    public void setVmSnapshotId(Guid snapshotId) {
        getImage().setSnapshotId(snapshotId);
    }

    public String getVmSnapshotDescription() {
        return vmSnapshotDescription;
    }

    public void setVmSnapshotDescription(String vmSnapshotDescription) {
        this.vmSnapshotDescription = vmSnapshotDescription;
    }

    public List<String> getStoragesNames() {
        return storagesNames;
    }

    public void setStoragesNames(List<String> storagesNames) {
        this.storagesNames = storagesNames;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    private double actualSize;

    /**
     * Get the actual Size of the DiskImage in GB.
     * The actual size is the size the DiskImage actually occupies on storage.
     * @return - Actual size used by this DiskImage in GB
     */
    public double getActualSize() {
        return actualSize;
    }

    public void setActualSize(double size) {
        actualSize = size;
    }

    public double getActualDiskWithSnapshotsSize() {
        if (actualDiskWithSnapthotsSize == 0 && snapshots != null) {
            for (DiskImage disk : snapshots) {
                actualDiskWithSnapthotsSize += disk.getActualSize();
            }
        }
        return actualDiskWithSnapthotsSize;
    }

    @JsonIgnore
    public double getActualDiskWithSnapshotsSizeInBytes() {
        return getActualDiskWithSnapshotsSize() * SizeConverter.BYTES_IN_GB;
    }

    public Object getQueryableId() {
        return getImageId();
    }

    public List<DiskImage> getSnapshots() {
        return snapshots;
    }

    public List<Guid> getQuotaIds() {
        return quotaIds;
    }

    public void setQuotaIds(List<Guid> quotaIds) {
        this.quotaIds = quotaIds;
    }

    public Guid getQuotaId() {
        if (quotaIds == null || quotaIds.isEmpty()) {
            return null;
        }
        return quotaIds.get(0);
    }

    public void setQuotaId(Guid quotaId) {
        quotaIds = new ArrayList<>();
        quotaIds.add(quotaId);
    }

    public List<String> getQuotaNames() {
        return quotaNames;
    }

    public void setQuotaNames(List<String> quotaNames) {
        this.quotaNames = quotaNames;
    }

    public String getQuotaName() {
        if (quotaNames == null || quotaNames.isEmpty()) {
            return null;
        }
        return quotaNames.get(0);
    }

    public List<Guid> getDiskProfileIds() {
        return diskProfileIds;
    }

    public void setDiskProfileIds(List<Guid> diskProfileIds) {
        this.diskProfileIds = diskProfileIds;
    }

    public List<String> getDiskProfileNames() {
        return diskProfileNames;
    }

    public void setDiskProfileNames(List<String> diskProfileNames) {
        this.diskProfileNames = diskProfileNames;
    }

    public Guid getDiskProfileId() {
        if (diskProfileIds == null || diskProfileIds.isEmpty()) {
            return null;
        }
        return diskProfileIds.get(0);
    }

    public void setDiskProfileId(Guid diskProfileId) {
        diskProfileIds = new ArrayList<>();
        diskProfileIds.add(diskProfileId);
    }

    public String getDiskProfileName() {
        if (diskProfileNames == null || diskProfileNames.isEmpty()) {
            return null;
        }
        return diskProfileNames.get(0);
    }

    public boolean hasRawBlock() {
        if (getVolumeFormat() != VolumeFormat.RAW) {
            return false;
        }
        for (StorageType type : getStorageTypes()) {
            if (type.isBlockDomain()) {
                return true;
            }
        }
        return false;
    }

    public static DiskImage copyOf(DiskImage diskImage) {
        DiskImage di = new DiskImage();

        // set all private fields (imitate clone - deep copy)
        di.setVolumeType(diskImage.getVolumeType());
        di.setVolumeFormat(diskImage.getVolumeFormat());
        di.setSize(diskImage.getSize());
        if (diskImage.getQuotaIds() != null) {
            di.setQuotaIds(new ArrayList<>(diskImage.getQuotaIds()));
        }
        if (diskImage.getQuotaNames() != null) {
            di.setQuotaNames(new ArrayList<>(diskImage.getQuotaNames()));
        }
        if (diskImage.getDiskProfileIds() != null) {
            di.setDiskProfileIds(new ArrayList<>(diskImage.getDiskProfileIds()));
        }
        if (diskImage.getDiskProfileNames() != null) {
            di.setDiskProfileNames(new ArrayList<>(diskImage.getDiskProfileNames()));
        }
        di.setQuotaEnforcementType(diskImage.getQuotaEnforcementType());
        di.setActive(diskImage.getActive());
        di.setCreationDate(new Date(diskImage.getCreationDate().getTime()));
        di.setSnapshotCreationDate(diskImage.getSnapshotCreationDate());
        di.setLastModifiedDate(new Date(diskImage.getLastModifiedDate().getTime()));
        di.actualSizeInBytes = diskImage.actualSizeInBytes;
        di.initialSizeInBytes = diskImage.initialSizeInBytes;
        di.readRateFromDiskImageDynamic = diskImage.readRateFromDiskImageDynamic;
        di.readOpsFromDiskImageDynamic = diskImage.readOpsFromDiskImageDynamic;
        di.writeRateFromDiskImageDynamic = diskImage.writeRateFromDiskImageDynamic;
        di.writeOpsFromDiskImageDynamic = diskImage.writeOpsFromDiskImageDynamic;
        di.readLatency = diskImage.readLatency;
        di.writeLatency = diskImage.writeLatency;
        di.flushLatency = diskImage.flushLatency;
        // string is immutable, so no need to deep copy it
        di.description = diskImage.description;
        di.setImageId(diskImage.getImageId());
        di.appList = diskImage.appList;
        di.setImageTemplateId(diskImage.getImageTemplateId());
        di.setParentId(diskImage.getParentId());
        di.setImageStatus(diskImage.getImageStatus());
        if (diskImage.getLastModified() != null) {
            di.setLastModified(new Date(diskImage.getLastModified().getTime()));
        }
        di.storageIds = new ArrayList<>(diskImage.storageIds);
        di.setVmSnapshotId(diskImage.getVmSnapshotId());
        di.setId(diskImage.getId());
        di.setNumberOfVms(diskImage.getNumberOfVms());
        di.setWipeAfterDelete(diskImage.isWipeAfterDelete());
        di.setPropagateErrors(diskImage.getPropagateErrors());
        di.setDiskAlias(diskImage.getDiskAlias());
        di.setDiskDescription(diskImage.getDiskDescription());
        di.setShareable(diskImage.isShareable());
        di.storagePoolId = diskImage.storagePoolId;
        di.actualSize = diskImage.actualSize;

        // TODO: is it ok to use shallow copy here?!
        di.snapshots = new ArrayList<>(diskImage.snapshots);
        di.actualDiskWithSnapthotsSize = diskImage.actualDiskWithSnapthotsSize;
        di.setCreationDate(new Date());
        di.setLastModified(new Date());
        di.setImageStatus(ImageStatus.LOCKED);
        di.setDiskProfileId(diskImage.getDiskProfileId());

        return di;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getImage(),
                snapshots,
                snapshotCreationDate,
                actualSizeInBytes,
                initialSizeInBytes,
                appList,
                description,
                readRateFromDiskImageDynamic,
                readOpsFromDiskImageDynamic,
                storageIds,
                storagePoolId,
                storagesNames,
                writeRateFromDiskImageDynamic,
                writeOpsFromDiskImageDynamic,
                readLatency,
                writeLatency,
                flushLatency,
                diskProfileIds,
                diskProfileNames
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiskImage)) {
            return false;
        }
        DiskImage other = (DiskImage) obj;
        return super.equals(obj)
                && Objects.equals(getImage(), other.getImage())
                && Objects.equals(snapshots, other.snapshots)
                && actualSizeInBytes == other.actualSizeInBytes
                && Objects.equals(initialSizeInBytes, other.initialSizeInBytes)
                && Objects.equals(appList, other.appList)
                && Objects.equals(description, other.description)
                && Objects.equals(snapshotCreationDate, other.getSnapshotCreationDate())
                && readRateFromDiskImageDynamic == other.readRateFromDiskImageDynamic
                && readOpsFromDiskImageDynamic == other.readOpsFromDiskImageDynamic
                && Objects.equals(storageIds, other.storageIds)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(storagesNames, other.storagesNames)
                && writeRateFromDiskImageDynamic == other.writeRateFromDiskImageDynamic
                && writeOpsFromDiskImageDynamic == other.writeOpsFromDiskImageDynamic
                && Objects.equals(readLatency, other.readLatency)
                && Objects.equals(writeLatency, other.writeLatency)
                && Objects.equals(flushLatency, other.flushLatency)
                && ObjectUtils.haveSameElements(diskProfileIds, other.diskProfileIds)
                && ObjectUtils.haveSameElements(diskProfileNames, other.diskProfileNames);
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

}
