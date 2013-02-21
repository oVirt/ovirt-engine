package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class DiskImage extends DiskImageBase implements IImage {

    private static final long serialVersionUID = 1533416252250153306L;

    private Date lastModifiedDate;
    private List<String> storagesNames;
    private long actualSizeFromDiskImageDynamic;
    private int readRateFromDiskImageDynamic;
    private int writeRateFromDiskImageDynamic;

    // Latency fields from DiskImageDynamic which are measured in seconds.
    private Double readLatency;
    private Double writeLatency;
    private Double flushLatency;

    private String appList;
    // TODO from storage_domain_static
    private NGuid storagePoolId;
    // TODO from storage_domain_static
    private ArrayList<String> storagePath;
    private int readRateKbPerSec;
    private int writeRateKbPerSec;
    private ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>();
    private double actualDiskWithSnapthotsSize;

    public DiskImage() {
        setParentId(Guid.Empty);
        setCreationDate(new Date());
        setLastModifiedDate(getCreationDate());
    }

    public DiskImage(DiskImageBase diskImageBase) {
        setParentId(Guid.Empty);
        setVolumeType(diskImageBase.getVolumeType());
        setvolumeFormat(diskImageBase.getVolumeFormat());
        setSize(diskImageBase.getSize());
        setDiskInterface(diskImageBase.getDiskInterface());
        setBoot(diskImageBase.isBoot());
        setWipeAfterDelete(diskImageBase.isWipeAfterDelete());
        setPropagateErrors(diskImageBase.getPropagateErrors());
        setQuotaId(diskImageBase.getQuotaId());
        setQuotaName(diskImageBase.getQuotaName());
        setQuotaEnforcementType(diskImageBase.getQuotaEnforcementType());
        setIsQuotaDefault(diskImageBase.isQuotaDefault());
    }

    public DiskImage(Boolean active,
            Date creation_date,
            Date last_modified_date,
            long actual_size,
            String description,
            Guid image_guid,
            String internal_drive_mapping,
            Guid it_guid,
            long size,
            Guid parentId,
            ImageStatus imageStatus,
            Date lastModified,
            String appList,
            VmEntityType vmEntityType,
            int numberOfVms,
            Guid quotaId,
            String quotaName,
            QuotaEnforcementTypeEnum quotaEnforcementType,
            boolean isQuotaDefault) {
        setActive(active);
        setCreationDate(creation_date);
        setLastModifiedDate(last_modified_date);
        actualSizeFromDiskImageDynamic = actual_size;
        setDescription(description);
        setImageId(image_guid);
        setImageTemplateId(it_guid);
        setSize(size);
        setParentId(parentId);
        setImageStatus(imageStatus);
        setLastModified(lastModified);
        setAppList(appList);
        setVmEntityType(vmEntityType);
        setNumberOfVms(numberOfVms);
        setQuotaId(quotaId);
        setQuotaName(quotaName);
        setQuotaEnforcementType(quotaEnforcementType);
        setIsQuotaDefault(isQuotaDefault);
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

    public long getActualSizeFromDiskImage() {
        return actualSizeFromDiskImageDynamic;
    }

    public void setActualSizeFromDiskImage(long size) {
        actualSizeFromDiskImageDynamic = size;
        setActualSize(getActualSizeFromDiskImage() * 1.0 / (1024 * 1024 * 1024));
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

    public ArrayList<Guid> getStorageIds() {
        return storageIds;
    }

    public void setStorageIds(ArrayList<Guid> storageIds) {
        this.storageIds = storageIds;
    }

    public NGuid getVmSnapshotId() {
        return getImage().getSnapshotId();
    }

    public void setVmSnapshotId(NGuid snapshotId) {
        getImage().setSnapshotId(snapshotId == null ? null : snapshotId.getValue());
    }

    public ArrayList<String> getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(ArrayList<String> storagePath) {
        this.storagePath = storagePath;
    }

    public List<String> getStoragesNames() {
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

    public NGuid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(NGuid storagePoolId) {
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

    public static DiskImage copyOf(DiskImage diskImage) {
        // set DiskImageBase properties
        DiskImage di = new DiskImage(diskImage);

        // set all private fields (imitate clone - deep copy)
        di.setActive(diskImage.getActive());
        di.setCreationDate(new Date(diskImage.getCreationDate().getTime()));
        di.setLastModifiedDate(new Date(diskImage.getLastModifiedDate().getTime()));
        di.actualSizeFromDiskImageDynamic = diskImage.actualSizeFromDiskImageDynamic;
        di.readRateFromDiskImageDynamic = diskImage.readRateFromDiskImageDynamic;
        di.writeRateFromDiskImageDynamic = diskImage.writeRateFromDiskImageDynamic;
        di.readLatency = diskImage.readLatency;
        di.writeLatency = diskImage.writeLatency;
        di.flushLatency = diskImage.flushLatency;
        // string is immutable, so no need to deep copy it
        di.description = diskImage.description;
        di.setImageId(new Guid(diskImage.getImageId().getUuid()));
        di.appList = diskImage.appList;
        di.setImageTemplateId(new Guid(diskImage.getImageTemplateId().getUuid()));
        di.setParentId(new Guid(diskImage.getParentId().getUuid()));
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
        di.storagePoolId = new NGuid(diskImage.storagePoolId.getUuid());
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
        result = prime * result + (int) (actualSizeFromDiskImageDynamic ^ (actualSizeFromDiskImageDynamic >>> 32));
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
        result = prime * result
                + ((flushLatency == null) ? 0 : flushLatency.hashCode());
        result = prime * result
                + ((readLatency == null) ? 0 : readLatency.hashCode());
        result = prime * result
                + ((writeLatency == null) ? 0 : writeLatency.hashCode());
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
                && actualSizeFromDiskImageDynamic == other.actualSizeFromDiskImageDynamic
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
                && ObjectUtils.objectsEqual(flushLatency, other.flushLatency));
    }

}
