package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class BrickPropertiesModel extends Model {

    private EntityModel status;
    private EntityModel port;
    private EntityModel pid;
    private EntityModel totalSize;
    private EntityModel freeSize;
    private EntityModel device;
    private EntityModel blockSize;
    private EntityModel mountOptions;
    private EntityModel fileSystem;

    public BrickPropertiesModel() {
        setStatus(new EntityModel());
        setPort(new EntityModel());
        setPid(new EntityModel());
        setTotalSize(new EntityModel());
        setFreeSize(new EntityModel());
        setDevice(new EntityModel());
        setBlockSize(new EntityModel());
        setMountOptions(new EntityModel());
        setFileSystem(new EntityModel());
    }

    public void setProperties(BrickProperties brickProperties) {
        getStatus().setEntity(brickProperties.getStatus());
        getPort().setEntity(brickProperties.getPort());
        getPid().setEntity(brickProperties.getPid());
        getTotalSize().setEntity(brickProperties.getTotalSize());
        getFreeSize().setEntity(brickProperties.getFreeSize());
        getDevice().setEntity(brickProperties.getDevice());
        getBlockSize().setEntity(brickProperties.getBlockSize());
        getMountOptions().setEntity(brickProperties.getMntOptions());
        getFileSystem().setEntity(brickProperties.getFsName());
    }

    public EntityModel getStatus() {
        return status;
    }

    public void setStatus(EntityModel status) {
        this.status = status;
    }

    public EntityModel getPort() {
        return port;
    }

    public void setPort(EntityModel port) {
        this.port = port;
    }

    public EntityModel getPid() {
        return pid;
    }

    public void setPid(EntityModel pid) {
        this.pid = pid;
    }

    public EntityModel getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(EntityModel totalSize) {
        this.totalSize = totalSize;
    }

    public EntityModel getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(EntityModel freeSize) {
        this.freeSize = freeSize;
    }

    public EntityModel getDevice() {
        return device;
    }

    public void setDevice(EntityModel device) {
        this.device = device;
    }

    public EntityModel getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(EntityModel blockSize) {
        this.blockSize = blockSize;
    }

    public EntityModel getMountOptions() {
        return mountOptions;
    }

    public void setMountOptions(EntityModel mountOptions) {
        this.mountOptions = mountOptions;
    }

    public EntityModel getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(EntityModel fileSystem) {
        this.fileSystem = fileSystem;
    }
}
