package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

public class DiskImage extends DiskImageBase implements IImage {

    private static final long serialVersionUID = 3185087852755356847L;

    private Date lastModifiedDate;
    private ArrayList<String> storagesNames;
    private long actualSizeInBytes;
    private int readRateFromDiskImageDynamic;
    private int writeRateFromDiskImageDynamic;

    // Latency fields from DiskImageDynamic which are measured in seconds.
    private Double readLatency;
    private Double writeLatency;
    private Double flushLatency;

    private String appList;
    // TODO from storage_domain_static
    private Guid storagePoolId;
    // TODO from storage_domain_static
    private ArrayList<String> storagePath;
    private int readRateKbPerSec;
    private int writeRateKbPerSec;
    private ArrayList<DiskImage> snapshots;
    private double actualDiskWithSnapthotsSize;
    private ArrayList<Guid> quotaIds;
    private ArrayList<String> quotaNames;
    private ArrayList<Guid> diskProfileIds;
    private ArrayList<String> diskProfileNames;
    private String vmSnapshotDescription;

    public DiskImage() {
        setParentId(Guid.Empty);
        setCreationDate(new Date());
        setLastModifiedDate(getCreationDate());
        snapshots = new ArrayList<DiskImage>();
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

    @Override
    public Date getCreationDate() {
        return getImage().getCreationDate();
    }

    @Override
    public void setCreationDate(Date creationDate) {
        getImage().setCreationDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public long getActualSizeInBytes() {
        return actualSizeInBytes;
    }

    public void setActualSizeInBytes(long size) {
        actualSizeInBytes = size;
        setActualSize(getActualSizeInBytes() * 1.0 / (1024 * 1024 * 1024));
    }

    public int getReadRate() {
        return readRateFromDiskImageDynamic;
    }

    public void setReadRate(int readRateFromDiskImageDynamic) {
        this.readRateFromDiskImageDynamic = readRateFromDiskImageDynamic;
    }

    public int getWriteRate() {
        return writeRateFromDiskImageDynamic;
    }

    public void setWriteRate(int writeRateFromDiskImageDynamic) {
        this.writeRateFromDiskImageDynamic = writeRateFromDiskImageDynamic;
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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppList() {
        return appList;
    }

    public void setAppList(String appList) {
        this.appList = appList;
    }

    @Override
    public Guid getImageTemplateId() {
        return getImage().getTemplateImageId();
    }

    @Override
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

    private ArrayList<Guid> storageIds;
    private ArrayList<StorageType> storageTypes;

    public ArrayList<Guid> getStorageIds() {
        return storageIds;
    }

    public void setStorageIds(ArrayList<Guid> storageIds) {
        this.storageIds = storageIds;
    }

    public ArrayList<StorageType> getStorageTypes() {
        return storageTypes;
    }

    public void setStorageTypes(ArrayList<StorageType> storageTypes) {
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

    public ArrayList<String> getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(ArrayList<String> storagePath) {
        this.storagePath = storagePath;
    }

    public ArrayList<String> getStoragesNames() {
        return storagesNames;
    }

    public void setStoragesNames(ArrayList<String> storagesNames) {
        this.storagesNames = storagesNames;
    }

    @Deprecated
    public Guid getimage_group_id() {
        return getId();
    }

    @Deprecated
    public void setimage_group_id(Guid value) {
        setId(value);
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

    /**
     * This method is created for SOAP serialization of primitives that are read only but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     * @param value
     */
    @Deprecated
    public void setActualDiskWithSnapshotsSize(double value) {

    }

    @Override
    public int getReadRateKbPerSec() {
        return readRateKbPerSec;
    }

    @Override
    public void setReadRateKbPerSec(int readRate) {
        readRateKbPerSec = readRate;
    }

    @Override
    public int getWriteRateKbPerSec() {
        return writeRateKbPerSec;
    }

    @Override
    public void setWriteRateKbPerSec(int writeRate) {
        writeRateKbPerSec = writeRate;
    }

    @Override
    public Object getQueryableId() {
        return getImageId();
    }

    public ArrayList<DiskImage> getSnapshots() {
        return snapshots;
    }

    public ArrayList<Guid> getQuotaIds() {
        return quotaIds;
    }

    public void setQuotaIds(ArrayList<Guid> quotaIds) {
        this.quotaIds = quotaIds;
    }

    public Guid getQuotaId() {
        if (quotaIds == null || quotaIds.isEmpty()) {
            return null;
        }
        return quotaIds.get(0);
    }

    public void setQuotaId(Guid quotaId) {
        quotaIds = new ArrayList<Guid>();
        quotaIds.add(quotaId);
    }

    public ArrayList<String> getQuotaNames() {
        return quotaNames;
    }

    public void setQuotaNames(ArrayList<String> quotaNames) {
        this.quotaNames = quotaNames;
    }

    public String getQuotaName() {
        if (quotaNames == null || quotaNames.isEmpty()) {
            return null;
        }
        return quotaNames.get(0);
    }

    public ArrayList<Guid> getDiskProfileIds() {
        return diskProfileIds;
    }

    public void setDiskProfileIds(ArrayList<Guid> diskProfileIds) {
        this.diskProfileIds = diskProfileIds;
    }

    public ArrayList<String> getDiskProfileNames() {
        return diskProfileNames;
    }

    public void setDiskProfileNames(ArrayList<String> diskProfileNames) {
        this.diskProfileNames = diskProfileNames;
    }

    public Guid getDiskProfileId() {
        if (diskProfileIds == null || diskProfileIds.isEmpty()) {
            return null;
        }
        return diskProfileIds.get(0);
    }

    public void setDiskProfileId(Guid diskProfileId) {
        diskProfileIds = new ArrayList<Guid>();
        diskProfileIds.add(diskProfileId);
    }

    public String getDiskProfileName() {
        if (diskProfileNames == null || diskProfileNames.isEmpty()) {
            return null;
        }
        return diskProfileNames.get(0);
    }

    public static DiskImage copyOf(DiskImage diskImage) {
        DiskImage di = new DiskImage();

        // set all private fields (imitate clone - deep copy)
        di.setVolumeType(diskImage.getVolumeType());
        di.setvolumeFormat(diskImage.getVolumeFormat());
        di.setSize(diskImage.getSize());
        di.setBoot(diskImage.isBoot());
        if (diskImage.getQuotaIds() != null) {
            di.setQuotaIds(new ArrayList<Guid>(diskImage.getQuotaIds()));
        }
        if (diskImage.getQuotaNames() != null) {
            di.setQuotaNames(new ArrayList<String>(diskImage.getQuotaNames()));
        }
        if (diskImage.getDiskProfileIds() != null) {
            di.setDiskProfileIds(new ArrayList<Guid>(diskImage.getDiskProfileIds()));
        }
        if (diskImage.getDiskProfileNames() != null) {
            di.setDiskProfileNames(new ArrayList<String>(diskImage.getDiskProfileNames()));
        }
        di.setQuotaEnforcementType(diskImage.getQuotaEnforcementType());
        di.setActive(diskImage.getActive());
        di.setCreationDate(new Date(diskImage.getCreationDate().getTime()));
        di.setLastModifiedDate(new Date(diskImage.getLastModifiedDate().getTime()));
        di.actualSizeInBytes = diskImage.actualSizeInBytes;
        di.readRateFromDiskImageDynamic = diskImage.readRateFromDiskImageDynamic;
        di.writeRateFromDiskImageDynamic = diskImage.writeRateFromDiskImageDynamic;
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
        di.setLastModified(new Date(diskImage.getLastModified().getTime()));
        di.storageIds = new ArrayList<Guid>(diskImage.storageIds);
        di.setVmSnapshotId(diskImage.getVmSnapshotId());
        di.storagePath = diskImage.storagePath;
        di.setId(diskImage.getId());
        di.setNumberOfVms(diskImage.getNumberOfVms());
        di.setDiskInterface(diskImage.getDiskInterface());
        di.setWipeAfterDelete(diskImage.isWipeAfterDelete());
        di.setPropagateErrors(diskImage.getPropagateErrors());
        di.setDiskAlias(diskImage.getDiskAlias());
        di.setDiskDescription(diskImage.getDiskDescription());
        di.setShareable(diskImage.isShareable());
        di.storagePoolId = diskImage.storagePoolId;
        di.actualSize = diskImage.actualSize;
        di.readRateKbPerSec = diskImage.readRateKbPerSec;
        di.writeRateKbPerSec = diskImage.writeRateKbPerSec;

        // TODO: is it ok to use shallow copy here?!
        di.snapshots = new ArrayList<DiskImage>(diskImage.snapshots);
        di.actualDiskWithSnapthotsSize = diskImage.actualDiskWithSnapthotsSize;
        di.setCreationDate(new Date());
        di.setLastModified(new Date());
        di.setActive(true);
        di.setImageStatus(ImageStatus.LOCKED);
        return di;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getImage() == null) ? 0 : getImage().hashCode());
        result = prime * result + ((snapshots == null) ? 0 : snapshots.hashCode());
        result = prime * result + (int) (actualSizeInBytes ^ (actualSizeInBytes >>> 32));
        result = prime * result + ((appList == null) ? 0 : appList.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + readRateKbPerSec;
        result = prime * result + writeRateKbPerSec;
        result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
        result = prime * result + readRateFromDiskImageDynamic;
        result = prime * result + ((storageIds == null) ? 0 : storageIds.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((storagesNames == null) ? 0 : storagesNames.hashCode());
        result = prime * result + writeRateFromDiskImageDynamic;
        result = prime * result + ((readLatency == null) ? 0 : readLatency.hashCode());
        result = prime * result + ((writeLatency == null) ? 0 : writeLatency.hashCode());
        result = prime * result + ((flushLatency == null) ? 0 : flushLatency.hashCode());
        result = prime * result + ((diskProfileIds == null) ? 0 : diskProfileIds.hashCode());
        result = prime * result + ((diskProfileNames == null) ? 0 : diskProfileNames.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DiskImage other = (DiskImage) obj;
        return (ObjectUtils.objectsEqual(getImage(), other.getImage())
                && ObjectUtils.objectsEqual(snapshots, other.snapshots)
                && actualSizeInBytes == other.actualSizeInBytes
                && ObjectUtils.objectsEqual(appList, other.appList)
                && ObjectUtils.objectsEqual(description, other.description)
                && readRateKbPerSec == other.readRateKbPerSec
                && writeRateKbPerSec == other.writeRateKbPerSec
                && ObjectUtils.objectsEqual(storagePath, other.storagePath)
                && readRateFromDiskImageDynamic == other.readRateFromDiskImageDynamic
                && ObjectUtils.objectsEqual(storageIds, other.storageIds)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && ObjectUtils.objectsEqual(storagesNames, other.storagesNames)
                && writeRateFromDiskImageDynamic == other.writeRateFromDiskImageDynamic
                && ObjectUtils.objectsEqual(readLatency, other.readLatency)
                && ObjectUtils.objectsEqual(writeLatency, other.writeLatency)
                && ObjectUtils.objectsEqual(flushLatency, other.flushLatency)
                && ObjectUtils.haveSameElements(diskProfileIds, other.diskProfileIds)
                && ObjectUtils.haveSameElements(diskProfileNames, other.diskProfileNames));
    }

}
