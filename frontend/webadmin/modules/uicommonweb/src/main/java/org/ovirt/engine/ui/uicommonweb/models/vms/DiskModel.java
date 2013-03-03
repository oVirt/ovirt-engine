package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class DiskModel extends Model
{
    private Disk disk;
    private VM vm;

    private EntityModel size;
    private EntityModel alias;
    private EntityModel description;
    private EntityModel sourceStorageDomainName;

    private ListModel volumeType;
    private ListModel diskInterface;
    private ListModel sourceStorageDomain;
    private ListModel storageDomain;
    private ListModel quota;

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public EntityModel getSize() {
        return size;
    }

    public void setSize(EntityModel size) {
        this.size = size;
    }

    public EntityModel getAlias() {
        return alias;
    }

    public void setAlias(EntityModel alias) {
        this.alias = alias;
    }

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    public EntityModel getSourceStorageDomainName() {
        return sourceStorageDomainName;
    }

    public void setSourceStorageDomainName(EntityModel sourceStorageDomainName) {
        this.sourceStorageDomainName = sourceStorageDomainName;
    }

    public ListModel getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(ListModel volumeType) {
        this.volumeType = volumeType;
    }

    public ListModel getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(ListModel diskInterface) {
        this.diskInterface = diskInterface;
    }

    public ListModel getSourceStorageDomain() {
        return sourceStorageDomain;
    }

    public void setSourceStorageDomain(ListModel sourceStorageDomain) {
        this.sourceStorageDomain = sourceStorageDomain;
    }

    public ListModel getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(ListModel storageDomain) {
        this.storageDomain = storageDomain;
    }

    public ListModel getQuota() {
        return quota;
    }

    public void setQuota(ListModel quota) {
        this.quota = quota;
    }

    public DiskModel() {
        setSize(new EntityModel());
        setAlias(new EntityModel());
        setDescription(new EntityModel());
        setSourceStorageDomainName(new EntityModel());
        setSourceStorageDomain(new ListModel());
        setDiskInterface(new ListModel());
        setStorageDomain(new ListModel());

        setQuota(new ListModel());
        getQuota().setIsAvailable(false);

        setVolumeType(new ListModel());
        getVolumeType().setItems(AsyncDataProvider.GetVolumeTypeList());
    }
}
