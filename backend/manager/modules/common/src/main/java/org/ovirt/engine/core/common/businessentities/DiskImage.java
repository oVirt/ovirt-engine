package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

public class DiskImage extends DiskImageBase implements INotifyPropertyChanged, IImage, Serializable {
    private static final long serialVersionUID = 1533416252250153306L;

    public DiskImage() {
        parentId = Guid.Empty;
        creation_dateField = new java.util.Date();
        last_modified_dateField = creation_dateField;
    }

    public DiskImage(Boolean active, java.util.Date creation_date, java.util.Date last_modified_date, long actual_size,
            String description, Guid image_guid, String internal_drive_mapping, Guid it_guid, long size, Guid vm_guid,
            Guid parentId, ImageStatus imageStatus, java.util.Date lastModified, String appList,VmEntityType vmEntityType, Guid quotaId, String quotaName) {
        this.activeField = active;
        this.creation_dateField = creation_date;
        this.setlast_modified_date(last_modified_date);
        this.actualSizeFromDiskImageDynamic = actual_size;
        this.description = description;
        this.id = image_guid;
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

    private VmEntityType vmEntityType;

    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    private Boolean activeField;
    private ArrayList<String> storagesNames;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((activeField == null) ? 0 : activeField.hashCode());
        result = prime * result
                + (int) (actualSizeFromDiskImageDynamic ^ (actualSizeFromDiskImageDynamic >>> 32));
        result = prime * result
                + ((appList == null) ? 0 : appList.hashCode());
        result = prime
                * result
                + ((creation_dateField == null) ? 0 : creation_dateField
                        .hashCode());
        result = prime
                * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime
                * result
                + ((status == null) ? 0 : status.hashCode());
        result = prime
                * result
                + ((getDisk() == null) ? 0 : getDisk()
                        .hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((it_guid == null) ? 0 : it_guid.hashCode());
        result = prime
                * result
                + ((lastModified == null) ? 0 : lastModified
                        .hashCode());
        result = prime * result
                + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + mReadRateKbPerSec;
        result = prime * result + mWriteRateKbPerSec;
        result = prime * result
                + ((storageIds == null) ? 0 : storageIds.hashCode());
        result = prime * result
                + ((mstorage_path == null) ? 0 : mstorage_path.hashCode());
        result = prime * result
                + ((vmSnapshotId == null) ? 0 : vmSnapshotId.hashCode());
        result = prime * result + readRateFromDiskImageDynamic;
        result = prime
                * result
                + ((storage_pool_idField == null) ? 0 : storage_pool_idField
                        .hashCode());
        result = prime * result
                + ((vm_guidField == null) ? 0 : vm_guidField.hashCode());
        result = prime * result + writeRateFromDiskImageDynamic;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiskImage other = (DiskImage) obj;
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
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (status != other.status)
            return false;
        if (getDisk() == null) {
            if (other.getDisk() != null)
                return false;
        } else if (!getDisk().equals(other.getDisk()))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
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
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (mReadRateKbPerSec != other.mReadRateKbPerSec)
            return false;
        if (mWriteRateKbPerSec != other.mWriteRateKbPerSec)
            return false;
        if (storageIds == null) {
            if (other.storageIds != null)
                return false;
        } else if (!storageIds.equals(other.storageIds))
            return false;
        if (mstorage_path == null) {
            if (other.mstorage_path != null)
                return false;
        } else if (!mstorage_path.equals(other.mstorage_path))
            return false;
        if (vmSnapshotId == null) {
            if (other.vmSnapshotId != null)
                return false;
        } else if (!vmSnapshotId.equals(other.vmSnapshotId))
            return false;
        if (readRateFromDiskImageDynamic != other.readRateFromDiskImageDynamic)
            return false;
        if (storage_pool_idField == null) {
            if (other.storage_pool_idField != null)
                return false;
        } else if (!storage_pool_idField.equals(other.storage_pool_idField))
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

    public Boolean getactive() {
        return this.activeField;
    }

    public void setactive(Boolean value) {
        this.activeField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("active"));
    }

    private java.util.Date creation_dateField;

    @Override
    public java.util.Date getcreation_date() {
        return this.creation_dateField;
    }

    @Override
    public void setcreation_date(java.util.Date value) {
        this.creation_dateField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("creation_date"));
    }

    // TODO why do we have two fields like this?
    private java.util.Date last_modified_dateField;

    public java.util.Date getlast_modified_date() {
        return this.last_modified_dateField;
    }

    public void setlast_modified_date(java.util.Date value) {
        this.last_modified_dateField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("last_modified_date"));
    }

    // TODO comes from DiskImageDynamic
    private long actualSizeFromDiskImageDynamic;

    public long getactual_size() {
        return this.actualSizeFromDiskImageDynamic;
    }

    public void setactual_size(long value) {
        this.actualSizeFromDiskImageDynamic = value;
        setActualSize(getactual_size() * 1.0 / (1024 * 1024 * 1024));
        OnPropertyChanged(new PropertyChangedEventArgs("actual_size"));
    }

    // TODO comes from DiskImageDynamic
    private int readRateFromDiskImageDynamic;

    public int getread_rate() {
        return this.readRateFromDiskImageDynamic;
    }

    public void setread_rate(int value) {
        this.readRateFromDiskImageDynamic = value;
        OnPropertyChanged(new PropertyChangedEventArgs("read_rate"));
    }

    // TODO comes from DiskImageDynamic
    private int writeRateFromDiskImageDynamic;

    public int getwrite_rate() {
        return this.writeRateFromDiskImageDynamic;
    }

    public void setwrite_rate(int value) {
        this.writeRateFromDiskImageDynamic = value;
        OnPropertyChanged(new PropertyChangedEventArgs("write_rate"));
    }

    private String description;

    @Override
    public String getdescription() {
        return this.description;
    }

    @Override
    public void setdescription(String value) {
        this.description = value;
        OnPropertyChanged(new PropertyChangedEventArgs("description"));
    }

    private Guid id = new Guid();

    private String appList;

    public String getappList() {
        return this.appList;
    }

    public void setappList(String value) {
        this.appList = value;
        OnPropertyChanged(new PropertyChangedEventArgs("appList"));
    }

    private Guid it_guid = Guid.Empty;

    @Override
    public Guid getit_guid() {
        return this.it_guid;
    }

    @Override
    public void setit_guid(Guid value) {
        this.it_guid = value;
        OnPropertyChanged(new PropertyChangedEventArgs("it_guid"));
    }

    // TODO comes from image_vm_map
    private Guid vm_guidField = new Guid();

    public Guid getvm_guid() {
        return this.vm_guidField;
    }

    public void setvm_guid(Guid value) {
        this.vm_guidField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vm_guid"));
    }

    private Guid parentId = new Guid();

    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid value) {
        parentId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("ParentId"));
    }

    private ImageStatus status = ImageStatus.Unassigned;

    public ImageStatus getimageStatus() {
        return this.status;
    }

    public void setimageStatus(ImageStatus value) {
        this.status = value;
        OnPropertyChanged(new PropertyChangedEventArgs("imageStatus"));
    }

    private java.util.Date lastModified = new java.util.Date(0);

    public java.util.Date getlastModified() {
        return this.lastModified;
    }

    public void setlastModified(java.util.Date value) {
        this.lastModified = value;
        OnPropertyChanged(new PropertyChangedEventArgs("lastModified"));
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
        OnPropertyChanged(new PropertyChangedEventArgs("vm_snapshot_id"));
    }

    // TODO from storage_domain_static
    private String mstorage_path;

    public String getstorage_path() {
        return mstorage_path;
    }

    public void setstorage_path(String value) {
        mstorage_path = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_path"));
    }

    public ArrayList<String> getStoragesNames() {
        return storagesNames;
    }

    public void setStoragesNames(ArrayList<String>  value) {
        storagesNames = value;
    }

    public Guid getimage_group_id() {
        return getDisk().getId();
    }

    public void setimage_group_id(Guid value) {
        getDisk().setId(value);
        OnPropertyChanged(new PropertyChangedEventArgs("image_group_id"));
    }

    // TODO from storage_domain_static
    private NGuid storage_pool_idField;

    public NGuid getstorage_pool_id() {
        return storage_pool_idField;
    }

    public void setstorage_pool_id(NGuid value) {
        storage_pool_idField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_pool_id"));
    }

    private double actualSize;

    public double getActualSize() {
        return actualSize;
    }

    public void setActualSize(double value) {
        actualSize = value;
        OnPropertyChanged(new PropertyChangedEventArgs("ActualSize"));
    }

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    private int mReadRateKbPerSec;

    private int mWriteRateKbPerSec;
    private java.util.ArrayList<DiskImage> _snapshots = new java.util.ArrayList<DiskImage>();

    public DiskImage(DiskImageBase diskImageBase) {
        parentId = Guid.Empty;
        setvolume_type(diskImageBase.getvolume_type());
        setvolume_format(diskImageBase.getvolume_format());
        setdisk_type(diskImageBase.getdisk_type());
        setsize(diskImageBase.getsize());
        setinternal_drive_mapping(diskImageBase.getinternal_drive_mapping());
        setdisk_interface(diskImageBase.getdisk_interface());
        setboot(diskImageBase.getboot());
        setwipe_after_delete(diskImageBase.getwipe_after_delete());
        setpropagate_errors(diskImageBase.getpropagate_errors());
        setQuotaId(diskImageBase.getQuotaId());
        setQuotaName(diskImageBase.getQuotaName());
    }

    private double _actualDiskWithSnapthotsSize;

    public double getActualDiskWithSnapshotsSize() {
        if (_actualDiskWithSnapthotsSize == 0 && _snapshots != null) {
            for (DiskImage disk : _snapshots) {
                _actualDiskWithSnapthotsSize += disk.getActualSize();
            }
        }
        return _actualDiskWithSnapthotsSize;
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
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
        return getId();
    }

    private static final java.util.ArrayList<String> _diskImageProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "active", "creation_date", "last_modified_date", "actual_size",
                    "description", "internal_drive_mapping", "appList", "it_guid", "vm_guid", "ParentId",
                    "imageStatus", "lastModified", "storage_id", "vm_snapshot_id", "storage_path", "image_group_id",
                    "storage_pool_id", "boot", "volume_type", "volume_format", "disk_interface", "wipe_after_delete",
                    "propagate_errors", "read_rate", "write_rate", "ActualSize", "QuotaId" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _diskImageProperties;
    }

    public java.util.ArrayList<DiskImage> getSnapshots() {
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
        di.id = new Guid(diskImage.id.getUuid());
        di.appList = diskImage.appList;
        di.it_guid = new Guid(diskImage.it_guid.getUuid());
        di.vm_guidField = new Guid(diskImage.vm_guidField.getUuid());
        di.parentId = new Guid(diskImage.parentId.getUuid());
        di.status = diskImage.status;
        di.lastModified = new Date(diskImage.lastModified.getTime());
        di.storageIds = new ArrayList<Guid>(diskImage.storageIds);
        di.vmSnapshotId = new NGuid(diskImage.vmSnapshotId.getUuid());
        di.mstorage_path = diskImage.mstorage_path;
        Disk otherDisk = diskImage.getDisk();
        di.setDisk(new Disk(otherDisk.getId(),
                otherDisk.getInternalDriveMapping(),
                otherDisk.getDiskType(),
                otherDisk.getDiskInterface(),
                otherDisk.isWipeAfterDelete(),
                otherDisk.getPropagateErrors()));
        di.storage_pool_idField = new NGuid(diskImage.storage_pool_idField.getUuid());
        di.actualSize = diskImage.actualSize;
        di.mReadRateKbPerSec = diskImage.mReadRateKbPerSec;
        di.mWriteRateKbPerSec = diskImage.mWriteRateKbPerSec;

        // TODO: is it ok to use shallow copy here?!
        di._snapshots = new java.util.ArrayList<DiskImage>(diskImage._snapshots);
        di._actualDiskWithSnapthotsSize = diskImage._actualDiskWithSnapthotsSize;
        di.setcreation_date(new java.util.Date());
        di.setlastModified(new java.util.Date());
        di.setactive(true);
        di.setimageStatus(ImageStatus.LOCKED);

        return di;
    }

    // TODO remove the follow APIs when the two classes are properly merged together

    @Override
    public VolumeType getvolume_type() {
        return super.getvolume_type();
    }

    @Override
    public void setvolume_type(VolumeType value) {
        super.setvolume_type(value);
    }

    @Override
    public VolumeFormat getvolume_format() {
        return super.getvolume_format();
    }

    @Override
    public void setvolume_format(VolumeFormat value) {
        super.setvolume_format(value);
    }

    @Override
    public long getsize() {
        return super.getsize();
    }

    @Override
    public void setsize(long value) {
        super.setsize(value);
    }

    @Override
    public boolean getboot() {
        return super.getboot();
    }

    @Override
    public void setboot(boolean value) {
        super.setboot(value);
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }
}
