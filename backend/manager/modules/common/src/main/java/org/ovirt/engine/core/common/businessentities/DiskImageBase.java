package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

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
    private String internalDriveMapping;
    private PropagateErrors propagateErrors = PropagateErrors.Off;
    private boolean boot;
    private boolean wipeAfterDelete;

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

    private DiskType mdisk_type;

    private DiskInterface diskInterface;


    public DiskImageBase() {
        size = 0;
        volumeType = VolumeType.Sparse;
        propagateErrors = PropagateErrors.Off;
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

    @XmlElement
    public DiskType getdisk_type() {
        return mdisk_type;
    }

    public void setdisk_type(DiskType value) {
        mdisk_type = value;
    }

    private long size = 0L;

    @XmlElement
    public long getsize() {
        return this.size;
    }

    public void setsize(long value) {
        this.size = value;
    }

    @XmlElement
    public String getinternal_drive_mapping() {
        return this.internalDriveMapping;
    }

    public void setinternal_drive_mapping(String value) {
        this.internalDriveMapping = value;
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

    @XmlElement
    public DiskInterface getdisk_interface() {
        return diskInterface;
    }

    public void setdisk_interface(DiskInterface value) {
        diskInterface = value;
    }

    @XmlElement
    public boolean getboot() {
        return boot;
    }

    public void setboot(boolean value) {
        boot = value;
    }

    @XmlElement
    public boolean getwipe_after_delete() {
        return wipeAfterDelete;
    }

    public void setwipe_after_delete(boolean value) {
        wipeAfterDelete = value;
    }

    @XmlElement
    public PropagateErrors getpropagate_errors() {
        return propagateErrors;
    }

    public void setpropagate_errors(PropagateErrors value) {
        propagateErrors = value;
    }

}
