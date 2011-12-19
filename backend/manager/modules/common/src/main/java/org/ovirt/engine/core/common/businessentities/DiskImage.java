package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskImage")
@Entity
@Table(name = "images")
@TypeDef(name = "guid", typeClass = GuidType.class)
@SecondaryTable(name = "disk_image_dynamic", pkJoinColumns = @PrimaryKeyJoinColumn(name = "image_id"))
public class DiskImage extends DiskImageBase implements INotifyPropertyChanged, IImage, Serializable {
    private static final long serialVersionUID = 1533416252250153306L;

    public DiskImage() {
        parentId = Guid.Empty;
        creation_dateField = new java.util.Date();
        last_modified_dateField = creation_dateField;
    }

    public DiskImage(Boolean active, java.util.Date creation_date, java.util.Date last_modified_date, long actual_size,
            String description, Guid image_guid, String internal_drive_mapping, Guid it_guid, long size, Guid vm_guid,
            Guid parentId, ImageStatus imageStatus, java.util.Date lastModified, String appList) {
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
    }

    // TODO comes from image_vm_map
    private Boolean activeField;

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
        result = prime * result + Arrays.hashCode(childrenIdField);
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
                + ((imageGroupId == null) ? 0 : imageGroupId
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
                + ((storageId == null) ? 0 : storageId.hashCode());
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
        if (!Arrays.equals(childrenIdField, other.childrenIdField))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (status != other.status)
            return false;
        if (imageGroupId == null) {
            if (other.imageGroupId != null)
                return false;
        } else if (!imageGroupId.equals(other.imageGroupId))
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
        if (storageId == null) {
            if (other.storageId != null)
                return false;
        } else if (!storageId.equals(other.storageId))
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(nillable = true)
    @Transient
    public Boolean getactive() {
        return this.activeField;
    }

    public void setactive(Boolean value) {
        this.activeField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("active"));
    }

    private java.util.Date creation_dateField;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Column(name = "creation_date", nullable = false)
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Transient
    public java.util.Date getlast_modified_date() {
        return this.last_modified_dateField;
    }

    public void setlast_modified_date(java.util.Date value) {
        this.last_modified_dateField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("last_modified_date"));
    }

    // TODO comes from DiskImageDynamic
    private long actualSizeFromDiskImageDynamic;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(table = "disk_image_dynamic", name = "actual_size", nullable = false)
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(table = "disk_image_dynamic", name = "read_rate")
    public int getread_rate() {
        return this.readRateFromDiskImageDynamic;
    }

    public void setread_rate(int value) {
        this.readRateFromDiskImageDynamic = value;
        OnPropertyChanged(new PropertyChangedEventArgs("read_rate"));
    }

    // TODO comes from DiskImageDynamic
    private int writeRateFromDiskImageDynamic;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(table = "disk_image_dynamic", name = "write_rate")
    public int getwrite_rate() {
        return this.writeRateFromDiskImageDynamic;
    }

    public void setwrite_rate(int value) {
        this.writeRateFromDiskImageDynamic = value;
        OnPropertyChanged(new PropertyChangedEventArgs("write_rate"));
    }

    private String description;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Column(name = "description", length = 4000)
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(name = "app_list", length = 4000)
    public String getappList() {
        return this.appList;
    }

    public void setappList(String value) {
        this.appList = value;
        OnPropertyChanged(new PropertyChangedEventArgs("appList"));
    }

    private Guid it_guid = new Guid();

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Column(name = "it_guid", nullable = false)
    @Type(type = "guid")
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Transient
    @Type(type = "guid")
    public Guid getvm_guid() {
        return this.vm_guidField;
    }

    public void setvm_guid(Guid value) {
        this.vm_guidField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vm_guid"));
    }

    private Guid parentId = new Guid();

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ParentId")
    @Column(name = "parentid")
    @Type(type = "guid")
    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid value) {
        parentId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("ParentId"));
    }

    private ImageStatus status = ImageStatus.Unassigned;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(name = "imagestatus")
    public ImageStatus getimageStatus() {
        return this.status;
    }

    public void setimageStatus(ImageStatus value) {
        this.status = value;
        OnPropertyChanged(new PropertyChangedEventArgs("imageStatus"));
    }

    private java.util.Date lastModified = new java.util.Date(0);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(name = "lastmodified")
    public java.util.Date getlastModified() {
        return this.lastModified;
    }

    public void setlastModified(java.util.Date value) {
        this.lastModified = value;
        OnPropertyChanged(new PropertyChangedEventArgs("lastModified"));
    }

    private NGuid storageId;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(name = "storage_id")
    @Type(type = "guid")
    public NGuid getstorage_id() {
        return storageId;
    }

    public void setstorage_id(NGuid value) {
        storageId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_id"));
    }

    private NGuid vmSnapshotId;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "vm_snapshot_id")
    @Column(name = "vm_snapshot_id")
    @Type(type = "guid")
    public NGuid getvm_snapshot_id() {
        return vmSnapshotId;
    }

    public void setvm_snapshot_id(NGuid value) {
        vmSnapshotId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vm_snapshot_id"));
    }

    // TODO from storage_domain_static
    private String mstorage_path;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Transient
    public String getstorage_path() {
        return mstorage_path;
    }

    public void setstorage_path(String value) {
        mstorage_path = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_path"));
    }

    private Guid imageGroupId = null;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Column(name = "image_group_id")
    @Type(type = "guid")
    public Guid getimage_group_id() {
        return imageGroupId;
    }

    public void setimage_group_id(Guid value) {
        imageGroupId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("image_group_id"));
    }

    // TODO from storage_domain_static
    private NGuid storage_pool_idField;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    @Transient
    public NGuid getstorage_pool_id() {
        return storage_pool_idField;
    }

    public void setstorage_pool_id(NGuid value) {
        storage_pool_idField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("storage_pool_id"));
    }

    private double actualSize;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ActualSize")
    @Transient
    public double getActualSize() {
        return actualSize;
    }

    public void setActualSize(double value) {
        actualSize = value;
        OnPropertyChanged(new PropertyChangedEventArgs("ActualSize"));
    }

    // C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    private Guid[] childrenIdField = new Guid[0];

    private int mReadRateKbPerSec;

    private int mWriteRateKbPerSec;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Snapshots")
    @Transient
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
    }

    @Transient
    public Guid[] getchildrenId() {
        return this.childrenIdField;
    }

    public void setchildrenId(Guid[] value) {
        this.childrenIdField = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    private double _actualDiskWithSnapthotsSize;

    @Transient
    @XmlElement(name = "ActualDiskWithSnapshotsSize")
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

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Transient
    public Guid getcontainer_guid() {
        return getvm_guid();
    }

    @Override
    public void setcontainer_guid(Guid value) {
        setvm_guid(value);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Transient
    public int getread_rate_kb_per_sec() {
        return mReadRateKbPerSec;
    }

    @Override
    public void setread_rate_kb_per_sec(int value) {
        mReadRateKbPerSec = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Override
    @XmlElement
    @Transient
    public int getwrite_rate_kb_per_sec() {
        return mWriteRateKbPerSec;
    }

    @Override
    public void setwrite_rate_kb_per_sec(int value) {
        mWriteRateKbPerSec = value;
    }

    @Override
    @Transient
    public Object getQueryableId() {
        return getId();
    }

    private static final java.util.ArrayList<String> _diskImageProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "active", "creation_date", "last_modified_date", "actual_size",
                    "description", "internal_drive_mapping", "appList", "it_guid", "vm_guid", "ParentId",
                    "imageStatus", "lastModified", "storage_id", "vm_snapshot_id", "storage_path", "image_group_id",
                    "storage_pool_id", "boot", "volume_type", "volume_format", "disk_interface", "wipe_after_delete",
                    "propagate_errors", "read_rate", "write_rate", "ActualSize" }));

    @Override
    @Transient
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _diskImageProperties;
    }

    @Transient
    public java.util.ArrayList<DiskImage> getSnapshots() {
        return _snapshots;
    }

    public static DiskImage copyOf(DiskImage diskImage) {
        // set DiskImageBase properties
        DiskImage di = new DiskImage(diskImage);

        // set all private fields (imitate clone)
        // pay attention the original java clone is not compatible with c# clone
        // by default,
        // since Date and Guid are shallow copied in C#, and in java we need to
        // deep copy them.
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
        di.storageId = new NGuid(diskImage.storageId.getUuid());
        di.vmSnapshotId = new NGuid(diskImage.vmSnapshotId.getUuid());
        di.mstorage_path = diskImage.mstorage_path;
        di.imageGroupId = new Guid(diskImage.imageGroupId.getUuid());
        di.storage_pool_idField = new NGuid(diskImage.storage_pool_idField.getUuid());
        di.actualSize = diskImage.actualSize;
        di.childrenIdField = new Guid[diskImage.childrenIdField.length];
        for (int i = 0; i < di.childrenIdField.length; i++) {
            di.childrenIdField[i] = new Guid(diskImage.childrenIdField[i].getUuid());
        }
        di.mReadRateKbPerSec = diskImage.mReadRateKbPerSec;
        di.mWriteRateKbPerSec = diskImage.mWriteRateKbPerSec;

        // TODO: is it ok to use shallow copy here?!
        di._snapshots = new java.util.ArrayList<DiskImage>(diskImage._snapshots);

        di._actualDiskWithSnapthotsSize = diskImage._actualDiskWithSnapthotsSize;

        // reset values which make were set in C# original DiskImage.clone()
        // method

        di.setcreation_date(new java.util.Date());
        di.setlastModified(new java.util.Date());
        di.setactive(true);
        di.setimageStatus(ImageStatus.LOCKED);

        return di;
    }

    // TODO remove the follow APIs when the two classes are properly merged together

    @Override
    @Column(name = "volume_type", nullable = false)
    public VolumeType getvolume_type() {
        return super.getvolume_type();
    }

    @Override
    public void setvolume_type(VolumeType value) {
        super.setvolume_type(value);
    }

    @Override
    @Column(name = "volume_format", nullable = false)
    @Enumerated
    public VolumeFormat getvolume_format() {
        return super.getvolume_format();
    }

    @Override
    public void setvolume_format(VolumeFormat value) {
        super.setvolume_format(value);
    }

    @Override
    @Column(name = "disk_type", nullable = false)
    @Enumerated
    public DiskType getdisk_type() {
        return super.getdisk_type();
    }

    @Override
    public void setdisk_type(DiskType value) {
        super.setdisk_type(value);
    }

    @Override
    @Column(name = "size", nullable = false)
    public long getsize() {
        return super.getsize();
    }

    @Override
    public void setsize(long value) {
        super.setsize(value);
    }

    @Override
    @Column(name = "internal_drive_mapping", length = 50)
    public String getinternal_drive_mapping() {
        return super.getinternal_drive_mapping();
    }

    @Override
    public void setinternal_drive_mapping(String value) {
        super.setinternal_drive_mapping(value);
    }

    @Override
    @Column(name = "disk_interface", nullable = false)
    @Enumerated
    public DiskInterface getdisk_interface() {
        return super.getdisk_interface();
    }

    @Override
    public void setdisk_interface(DiskInterface value) {
        super.setdisk_interface(value);
    }

    @Override
    @Column(name = "boot")
    public boolean getboot() {
        return super.getboot();
    }

    @Override
    public void setboot(boolean value) {
        super.setboot(value);
    }

    @Override
    @Column(name = "wipe_after_delete", nullable = false)
    public boolean getwipe_after_delete() {
        return super.getwipe_after_delete();
    }

    @Override
    public void setwipe_after_delete(boolean value) {
        super.setwipe_after_delete(value);
    }

    @Override
    @Column(name = "propagate_errors", nullable = false)
    @Enumerated
    public PropagateErrors getpropagate_errors() {
        return super.getpropagate_errors();
    }

    @Override
    public void setpropagate_errors(PropagateErrors value) {
        super.setpropagate_errors(value);
    }

    @Override
    @XmlElement(name="Id")
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "image_guid")
    @Type(type = "guid")
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }
}
