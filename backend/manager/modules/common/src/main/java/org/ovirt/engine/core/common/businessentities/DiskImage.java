package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;

public class DiskImage extends DiskImageBase implements INotifyPropertyChanged, IImage {

    private static final long serialVersionUID = 1533416252250153306L;

    private static final java.util.ArrayList<String> _diskImageProperties = new ArrayList<String>(
            java.util.Arrays.asList(new String[] { "active", "creation_date", "last_modified_date", "actual_size",
                    "description", "internal_drive_mapping", "appList", "it_guid", "vm_guid", "ParentId",
                    "imageStatus", "lastModified", "storage_id", "vm_snapshot_id", "storage_path", "image_group_id",
                    "storage_pool_id", "boot", "volume_type", "volume_format", "disk_interface", "wipe_after_delete",
                    "propagate_errors", "read_rate", "write_rate", "ActualSize", "QuotaId" }));

    private Boolean activeField;
    private ArrayList<String> storagesNames;
    // TODO why do we have two fields like this?
    private Date last_modified_dateField;
    private Date lastModified = new Date(0);
    // TODO comes from DiskImageDynamic
    private long actualSizeFromDiskImageDynamic;
    // TODO comes from DiskImageDynamic
    private int readRateFromDiskImageDynamic;
    // TODO comes from DiskImageDynamic
    private int writeRateFromDiskImageDynamic;
    private Guid imageId = Guid.Empty;
    private String appList;
    // TODO comes from image_vm_map
    private Guid vm_guidField = Guid.Empty;
    private Guid parentId = Guid.Empty;
    private Guid it_guid = Guid.Empty;
    // TODO from storage_domain_static
    private NGuid storage_pool_idField;
    private ImageStatus status = ImageStatus.Unassigned;
    private Date creation_dateField;
    // TODO from storage_domain_static
    private ArrayList<String> mstorage_path;
    private int mReadRateKbPerSec;
    private int mWriteRateKbPerSec;
    private ArrayList<DiskImage> _snapshots = new ArrayList<DiskImage>();
    private double _actualDiskWithSnapthotsSize;

    public DiskImage() {
        parentId = Guid.Empty;
        creation_dateField = new Date();
        last_modified_dateField = creation_dateField;
    }

    public DiskImage(DiskImageBase diskImageBase) {
        parentId = Guid.Empty;
        setvolume_type(diskImageBase.getvolume_type());
        setvolume_format(diskImageBase.getvolume_format());
        setsize(diskImageBase.getsize());
        setinternal_drive_mapping(diskImageBase.getinternal_drive_mapping());
        setDiskInterface(diskImageBase.getDiskInterface());
        setboot(diskImageBase.getboot());
        setWipeAfterDelete(diskImageBase.isWipeAfterDelete());
        setPropagateErrors(diskImageBase.getPropagateErrors());
        setQuotaId(diskImageBase.getQuotaId());
        setQuotaName(diskImageBase.getQuotaName());
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
            Guid vm_guid,
            Guid parentId,
            ImageStatus imageStatus,
            Date lastModified,
            String appList,
            VmEntityType vmEntityType,
            Guid quotaId,
            String quotaName) {
        this.activeField = active;
        this.creation_dateField = creation_date;
        this.setlast_modified_date(last_modified_date);
        this.actualSizeFromDiskImageDynamic = actual_size;
        this.description = description;
        this.imageId = image_guid;
        this.setinternal_drive_mapping(internal_drive_mapping);
        this.it_guid = it_guid;
        this.setsize(size);
        this.vm_guidField = vm_guid;
        this.setParentId(parentId);
        this.setimageStatus(imageStatus);
        this.setlastModified(lastModified);
        this.setappList(appList);
        this.setVmEntityType(vmEntityType);
        this.setQuotaId(quotaId);
        this.setQuotaName(quotaName);
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid id) {
        this.imageId = id;
    }

    private VmEntityType vmEntityType;

    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    public Boolean getactive() {
        return this.activeField;
    }

    public void setactive(Boolean value) {
        this.activeField = value;
    }

    @Override
    public Date getcreation_date() {
        return this.creation_dateField;
    }

    @Override
    public void setcreation_date(Date value) {
        this.creation_dateField = value;
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
        return this.it_guid;
    }

    @Override
    public void setit_guid(Guid value) {
        this.it_guid = value;
    }

    public Guid getvm_guid() {
        return this.vm_guidField;
    }

    public void setvm_guid(Guid value) {
        this.vm_guidField = value;
    }

    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid value) {
        parentId = value;
    }

    public ImageStatus getimageStatus() {
        return this.status;
    }

    public void setimageStatus(ImageStatus value) {
        this.status = value;
    }

    public Date getlastModified() {
        return this.lastModified;
    }

    public void setlastModified(Date value) {
        this.lastModified = value;
    }

    private ArrayList<Guid> storageIds;

    public ArrayList<Guid> getstorage_ids() {
        return storageIds;
    }

    public void setstorage_ids(ArrayList<Guid> value) {
        storageIds = value;
    }

    private NGuid vmSnapshotId;

    public NGuid getvm_snapshot_id() {
        return vmSnapshotId;
    }

    public void setvm_snapshot_id(NGuid value) {
        vmSnapshotId = value;
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
    public Guid getcontainer_guid() {
        return getvm_guid();
    }

    @Override
    public void setcontainer_guid(Guid value) {
        setvm_guid(value);
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

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _diskImageProperties;
    }

    public ArrayList<DiskImage> getSnapshots() {
        return _snapshots;
    }

    public static DiskImage copyOf(DiskImage diskImage) {
        // set DiskImageBase properties
        DiskImage di = new DiskImage(diskImage);

        // set all private fields (imitate clone - deep copy)
        di.activeField = diskImage.activeField;
        di.creation_dateField = new java.util.Date(diskImage.creation_dateField.getTime());
        di.last_modified_dateField = new java.util.Date(diskImage.last_modified_dateField.getTime());
        di.actualSizeFromDiskImageDynamic = diskImage.actualSizeFromDiskImageDynamic;
        di.readRateFromDiskImageDynamic = diskImage.readRateFromDiskImageDynamic;
        di.writeRateFromDiskImageDynamic = diskImage.writeRateFromDiskImageDynamic;
        // string is immutable, so no need to deep copy it
        di.description = diskImage.description;
        di.imageId = new Guid(diskImage.imageId.getUuid());
        di.appList = diskImage.appList;
        di.it_guid = new Guid(diskImage.it_guid.getUuid());
        di.vm_guidField = new Guid(diskImage.vm_guidField.getUuid());
        di.parentId = new Guid(diskImage.parentId.getUuid());
        di.status = diskImage.status;
        di.lastModified = new Date(diskImage.lastModified.getTime());
        di.storageIds = new ArrayList<Guid>(diskImage.storageIds);
        di.vmSnapshotId = new NGuid(diskImage.vmSnapshotId.getUuid());
        di.mstorage_path = diskImage.mstorage_path;
        di.setId(diskImage.getId());
        di.setinternal_drive_mapping(diskImage.getinternal_drive_mapping());
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
        di.setimageStatus(ImageStatus.LOCKED);

        return di;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_snapshots == null) ? 0 : _snapshots.hashCode());
        result = prime * result + ((activeField == null) ? 0 : activeField.hashCode());
        result = prime * result + (int) (actualSizeFromDiskImageDynamic ^ (actualSizeFromDiskImageDynamic >>> 32));
        result = prime * result + ((appList == null) ? 0 : appList.hashCode());
        result = prime * result + ((creation_dateField == null) ? 0 : creation_dateField.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
        result = prime * result + ((it_guid == null) ? 0 : it_guid.hashCode());
        result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
        result = prime * result + mReadRateKbPerSec;
        result = prime * result + mWriteRateKbPerSec;
        result = prime * result + ((mstorage_path == null) ? 0 : mstorage_path.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + readRateFromDiskImageDynamic;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((storageIds == null) ? 0 : storageIds.hashCode());
        result = prime * result + ((storage_pool_idField == null) ? 0 : storage_pool_idField.hashCode());
        result = prime * result + ((storagesNames == null) ? 0 : storagesNames.hashCode());
        result = prime * result + ((vmSnapshotId == null) ? 0 : vmSnapshotId.hashCode());
        result = prime * result + ((vm_guidField == null) ? 0 : vm_guidField.hashCode());
        result = prime * result + writeRateFromDiskImageDynamic;
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
        if (_snapshots == null) {
            if (other._snapshots != null)
                return false;
        } else if (!_snapshots.equals(other._snapshots))
            return false;
        if (activeField == null) {
            if (other.activeField != null)
                return false;
        } else if (!activeField.equals(other.activeField))
            return false;
        if (actualSizeFromDiskImageDynamic != other.actualSizeFromDiskImageDynamic)
            return false;
        if (appList == null) {
            if (other.appList != null)
                return false;
        } else if (!appList.equals(other.appList))
            return false;
        if (creation_dateField == null) {
            if (other.creation_dateField != null)
                return false;
        } else if (!creation_dateField.equals(other.creation_dateField))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (imageId == null) {
            if (other.imageId != null)
                return false;
        } else if (!imageId.equals(other.imageId))
            return false;
        if (it_guid == null) {
            if (other.it_guid != null)
                return false;
        } else if (!it_guid.equals(other.it_guid))
            return false;
        if (lastModified == null) {
            if (other.lastModified != null)
                return false;
        } else if (!lastModified.equals(other.lastModified))
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
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (readRateFromDiskImageDynamic != other.readRateFromDiskImageDynamic)
            return false;
        if (status != other.status)
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
        if (vmSnapshotId == null) {
            if (other.vmSnapshotId != null)
                return false;
        } else if (!vmSnapshotId.equals(other.vmSnapshotId))
            return false;
        if (vm_guidField == null) {
            if (other.vm_guidField != null)
                return false;
        } else if (!vm_guidField.equals(other.vm_guidField))
            return false;
        if (writeRateFromDiskImageDynamic != other.writeRateFromDiskImageDynamic)
            return false;
        return true;
    }

}
