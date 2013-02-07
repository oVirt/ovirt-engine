package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class DiskImage extends DiskImageBase implements IImage {

    private static final long serialVersionUID = 1533416252250153306L;

    private ArrayList<String> storagesNames;
    // TODO why do we have two fields like this?
    private Date last_modified_dateField;
    // TODO comes from DiskImageDynamic
    private long actualSizeFromDiskImageDynamic;
    // TODO comes from DiskImageDynamic
    private int readRateFromDiskImageDynamic;
    // TODO comes from DiskImageDynamic
    private int writeRateFromDiskImageDynamic;

    // Latency fields from DiskImageDynamic which are measured in seconds.
    private Double readLatency;
    private Double writeLatency;
    private Double flushLatency;

    private String appList;
    // TODO from storage_domain_static
    private NGuid storage_pool_idField;
    // TODO from storage_domain_static
    private ArrayList<String> mstorage_path;
    private int mReadRateKbPerSec;
    private int mWriteRateKbPerSec;
    private ArrayList<DiskImage> _snapshots = new ArrayList<DiskImage>();
    private double _actualDiskWithSnapthotsSize;

    public DiskImage() {
        setParentId(Guid.Empty);
        setcreation_date(new Date());
        setlast_modified_date(getcreation_date());
    }

    public DiskImage(DiskImageBase diskImageBase) {
        setParentId(Guid.Empty);
        setvolume_type(diskImageBase.getvolume_type());
        setvolume_format(diskImageBase.getvolume_format());
        setsize(diskImageBase.getsize());
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
        setactive(active);
        setcreation_date(creation_date);
        this.setlast_modified_date(last_modified_date);
        this.actualSizeFromDiskImageDynamic = actual_size;
        this.description = description;
        setImageId(image_guid);
        setit_guid(it_guid);
        this.setsize(size);
        this.setParentId(parentId);
        this.setImageStatus(imageStatus);
        this.setlastModified(lastModified);
        this.setappList(appList);
        this.setVmEntityType(vmEntityType);
        this.setNumberOfVms(numberOfVms);
        this.setQuotaId(quotaId);
        this.setQuotaName(quotaName);
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

    public Boolean getactive() {
        return getImage().isActive();
    }

    public void setactive(Boolean value) {
        getImage().setActive(Boolean.TRUE.equals(value));
    }

    @Override
    public Date getcreation_date() {
        return getImage().getCreationDate();
    }

    @Override
    public void setcreation_date(Date value) {
        getImage().setCreationDate(value);
    }

    public Date getlast_modified_date() {
        return this.last_modified_dateField;
    }

    public void setlast_modified_date(Date value) {
        this.last_modified_dateField = value;
    }

    public long getactual_size() {
        return this.actualSizeFromDiskImageDynamic;
    }

    public void setactual_size(long value) {
        this.actualSizeFromDiskImageDynamic = value;
        setActualSize(getactual_size() * 1.0 / (1024 * 1024 * 1024));
    }

    public int getread_rate() {
        return this.readRateFromDiskImageDynamic;
    }

    public void setread_rate(int value) {
        this.readRateFromDiskImageDynamic = value;
    }

    public int getwrite_rate() {
        return this.writeRateFromDiskImageDynamic;
    }

    public void setwrite_rate(int value) {
        this.writeRateFromDiskImageDynamic = value;
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
    public String getdescription() {
        return this.description;
    }

    @Override
    public void setdescription(String value) {
        this.description = value;
    }

    public String getappList() {
        return this.appList;
    }

    public void setappList(String value) {
        this.appList = value;
    }

    @Override
    public Guid getit_guid() {
        return getImage().getTemplateImageId();
    }

    @Override
    public void setit_guid(Guid value) {
        getImage().setTemplateImageId(value);
    }

    public Guid getParentId() {
        return getImage().getParentId();
    }

    public void setParentId(Guid value) {
        getImage().setParentId(value);
    }

    public ImageStatus getImageStatus() {
        return getImage().getStatus();
    }

    public void setImageStatus(ImageStatus value) {
        getImage().setStatus(value);
    }

    public Date getlastModified() {
        return getImage().getLastModified();
    }

    public void setlastModified(Date value) {
        getImage().setLastModified(value);
    }

    private ArrayList<Guid> storageIds;

    public ArrayList<Guid> getstorage_ids() {
        return storageIds;
    }

    public void setstorage_ids(ArrayList<Guid> value) {
        storageIds = value;
    }

    public NGuid getvm_snapshot_id() {
        return getImage().getSnapshotId();
    }

    public void setvm_snapshot_id(NGuid value) {
        getImage().setSnapshotId(value == null ? null : value.getValue());
    }

    public ArrayList<String> getstorage_path() {
        return mstorage_path;
    }

    public void setstorage_path(ArrayList<String> value) {
        mstorage_path = value;
    }

    public ArrayList<String> getStoragesNames() {
        return storagesNames;
    }

    public void setStoragesNames(ArrayList<String> value) {
        storagesNames = value;
    }

    @Deprecated
    public Guid getimage_group_id() {
        return getId();
    }

    @Deprecated
    public void setimage_group_id(Guid value) {
        setId(value);
    }

    public NGuid getstorage_pool_id() {
        return storage_pool_idField;
    }

    public void setstorage_pool_id(NGuid value) {
        storage_pool_idField = value;
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

    public void setActualSize(double value) {
        actualSize = value;
    }

    public double getActualDiskWithSnapshotsSize() {
        if (_actualDiskWithSnapthotsSize == 0 && _snapshots != null) {
            for (DiskImage disk : _snapshots) {
                _actualDiskWithSnapthotsSize += disk.getActualSize();
            }
        }
        return _actualDiskWithSnapthotsSize;
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
    public int getread_rate_kb_per_sec() {
        return mReadRateKbPerSec;
    }

    @Override
    public void setread_rate_kb_per_sec(int value) {
        mReadRateKbPerSec = value;
    }

    @Override
    public int getwrite_rate_kb_per_sec() {
        return mWriteRateKbPerSec;
    }

    @Override
    public void setwrite_rate_kb_per_sec(int value) {
        mWriteRateKbPerSec = value;
    }

    @Override
    public Object getQueryableId() {
        return getImageId();
    }

    public ArrayList<DiskImage> getSnapshots() {
        return _snapshots;
    }

    public static DiskImage copyOf(DiskImage diskImage) {
        // set DiskImageBase properties
        DiskImage di = new DiskImage(diskImage);

        // set all private fields (imitate clone - deep copy)
        di.setactive(diskImage.getactive());
        di.setcreation_date(new Date(diskImage.getcreation_date().getTime()));
        di.setlast_modified_date(new Date(diskImage.getlast_modified_date().getTime()));
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
        di.setit_guid(new Guid(diskImage.getit_guid().getUuid()));
        di.setParentId(new Guid(diskImage.getParentId().getUuid()));
        di.setImageStatus(diskImage.getImageStatus());
        di.setlastModified(new Date(diskImage.getlastModified().getTime()));
        di.storageIds = new ArrayList<Guid>(diskImage.storageIds);
        di.setvm_snapshot_id(diskImage.getvm_snapshot_id());
        di.mstorage_path = diskImage.mstorage_path;
        di.setId(diskImage.getId());
        di.setNumberOfVms(diskImage.getNumberOfVms());
        di.setDiskInterface(diskImage.getDiskInterface());
        di.setWipeAfterDelete(diskImage.isWipeAfterDelete());
        di.setPropagateErrors(diskImage.getPropagateErrors());
        di.setDiskAlias(diskImage.getDiskAlias());
        di.setDiskDescription(diskImage.getDiskDescription());
        di.setShareable(diskImage.isShareable());
        di.storage_pool_idField = new NGuid(diskImage.storage_pool_idField.getUuid());
        di.actualSize = diskImage.actualSize;
        di.mReadRateKbPerSec = diskImage.mReadRateKbPerSec;
        di.mWriteRateKbPerSec = diskImage.mWriteRateKbPerSec;

        // TODO: is it ok to use shallow copy here?!
        di._snapshots = new ArrayList<DiskImage>(diskImage._snapshots);
        di._actualDiskWithSnapthotsSize = diskImage._actualDiskWithSnapthotsSize;
        di.setcreation_date(new Date());
        di.setlastModified(new Date());
        di.setactive(true);
        di.setImageStatus(ImageStatus.LOCKED);

        return di;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getImage() == null) ? 0 : getImage().hashCode());
        result = prime * result + ((_snapshots == null) ? 0 : _snapshots.hashCode());
        result = prime * result + (int) (actualSizeFromDiskImageDynamic ^ (actualSizeFromDiskImageDynamic >>> 32));
        result = prime * result + ((appList == null) ? 0 : appList.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + mReadRateKbPerSec;
        result = prime * result + mWriteRateKbPerSec;
        result = prime * result + ((mstorage_path == null) ? 0 : mstorage_path.hashCode());
        result = prime * result + readRateFromDiskImageDynamic;
        result = prime * result + ((storageIds == null) ? 0 : storageIds.hashCode());
        result = prime * result + ((storage_pool_idField == null) ? 0 : storage_pool_idField.hashCode());
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
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiskImage other = (DiskImage) obj;
        if (getImage() == null) {
            if (other.getImage() != null)
                return false;
        } else if (!getImage().equals(other.getImage()))
            return false;
        if (_snapshots == null) {
            if (other._snapshots != null)
                return false;
        } else if (!_snapshots.equals(other._snapshots))
            return false;
        if (actualSizeFromDiskImageDynamic != other.actualSizeFromDiskImageDynamic)
            return false;
        if (appList == null) {
            if (other.appList != null)
                return false;
        } else if (!appList.equals(other.appList))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (mReadRateKbPerSec != other.mReadRateKbPerSec)
            return false;
        if (mWriteRateKbPerSec != other.mWriteRateKbPerSec)
            return false;
        if (mstorage_path == null) {
            if (other.mstorage_path != null)
                return false;
        } else if (!mstorage_path.equals(other.mstorage_path))
            return false;
        if (readRateFromDiskImageDynamic != other.readRateFromDiskImageDynamic)
            return false;
        if (storageIds == null) {
            if (other.storageIds != null)
                return false;
        } else if (!storageIds.equals(other.storageIds))
            return false;
        if (storage_pool_idField == null) {
            if (other.storage_pool_idField != null)
                return false;
        } else if (!storage_pool_idField.equals(other.storage_pool_idField))
            return false;
        if (storagesNames == null) {
            if (other.storagesNames != null)
                return false;
        } else if (!storagesNames.equals(other.storagesNames))
            return false;
        if (writeRateFromDiskImageDynamic != other.writeRateFromDiskImageDynamic)
            return false;
        if (readLatency == null) {
            if (other.readLatency != null)
                return false;
        } else if (!readLatency.equals(other.readLatency))
            return false;
        if (writeLatency == null) {
            if (other.writeLatency != null)
                return false;
        } else if (!writeLatency.equals(other.writeLatency))
            return false;
        if (flushLatency == null) {
            if (other.flushLatency != null)
                return false;
        } else if (!flushLatency.equals(other.flushLatency))
            return false;
        return true;
    }

}
