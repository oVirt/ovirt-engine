package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskImageBase")
public class DiskImageBase extends IVdcQueryable implements Serializable {

    private static final long serialVersionUID = 4913899921353163969L;

    @NotNull(message = "VALIDATION.VOLUME_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType = VolumeType.Sparse;
    private boolean boot;

    @Valid
    private Disk disk = new Disk();

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;



    public DiskImageBase() {
        size = 0;
        volumeType = VolumeType.Sparse;
    }

    @XmlElement
    public VolumeType getvolume_type() {
        return volumeType;
    }

    public void setvolume_type(VolumeType value) {
        volumeType = value;
    }

    @XmlElement
    public VolumeFormat getvolume_format() {
        return volumeFormat;
    }

    public void setvolume_format(VolumeFormat value) {
        volumeFormat = value;
    }

    public DiskType getdisk_type() {
        return disk.getDiskType();
    }

    public void setdisk_type(DiskType value) {
        disk.setDiskType(value);
    }

    private long size = 0L;

    @XmlElement
    public long getsize() {
        return this.size;
    }

    public void setsize(long value) {
        this.size = value;
    }

    public String getinternal_drive_mapping() {
        return Integer.toString(disk.getInternalDriveMapping());
    }

    public void setinternal_drive_mapping(String value) {
        disk.setInternalDriveMapping(Integer.parseInt(value));
    }

    /**
     * disk size in GB
     */
    @XmlElement
    public long getSizeInGigabytes() {
        return getsize() / (1024 * 1024 * 1024);
    }

    public void setSizeInGigabytes(long value) {
        setsize(value * (1024 * 1024 * 1024));
    }

    public DiskInterface getdisk_interface() {
        return disk.getDiskInterface();
    }

    public void setdisk_interface(DiskInterface value) {
        disk.setDiskInterface(value);
    }

    @XmlElement
    public boolean getboot() {
        return boot;
    }

    public void setboot(boolean value) {
        boot = value;
    }

    public boolean getwipe_after_delete() {
        return disk.isWipeAfterDelete();
    }

    public void setwipe_after_delete(boolean value) {
        disk.setWipeAfterDelete(value);
    }

    public PropagateErrors getpropagate_errors() {
        return disk.getPropagateErrors();
    }

    public void setpropagate_errors(PropagateErrors value) {
        disk.setPropagateErrors(value);
    }

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }
}
