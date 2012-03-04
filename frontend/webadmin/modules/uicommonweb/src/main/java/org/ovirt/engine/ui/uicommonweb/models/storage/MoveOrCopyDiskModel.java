package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public abstract class MoveOrCopyDiskModel extends DisksAllocationModel implements ICommandTarget
{
    private ArrayList<DiskModel> templateDisks;

    public ArrayList<DiskModel> getTemplateDisks()
    {
        return templateDisks;
    }

    public void setTemplateDisks(ArrayList<DiskModel> value)
    {
        if (templateDisks != value)
        {
            templateDisks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Template Disks"));
        }
    }

    private ArrayList<storage_domains> storageDomains;

    public ArrayList<storage_domains> getStorageDomains()
    {
        return storageDomains;
    }

    public void setStorageDomains(ArrayList<storage_domains> value)
    {
        if (storageDomains != value)
        {
            storageDomains = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Storage Domains"));
        }
    }

    private ArrayList<DiskImage> diskImages;

    public ArrayList<DiskImage> getDiskImages()
    {
        return diskImages;
    }

    public void setDiskImages(ArrayList<DiskImage> value)
    {
        if (diskImages != value)
        {
            diskImages = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disk Images"));
        }
    }

    protected ArrayList<storage_domains> activeStorageDomains;
    protected ArrayList<storage_domains> disjointStorageDomains;
    protected ArrayList<storage_domains> unionStorageDomains;

    public abstract void init(ArrayList<DiskImage> diskImages);

    protected abstract void initStorageDomains();

    protected abstract void PostCopyOrMoveInit();

    public MoveOrCopyDiskModel() {
        templateDisks = new ArrayList<DiskModel>();
        storageDomains = new ArrayList<storage_domains>();
    }

    protected void onInitDisks() {
        ArrayList<DiskModel> disks = new ArrayList<DiskModel>();
        for (DiskImage disk : getDiskImages())
        {
            disks.add(diskImageToModel(disk));
        }
        setDisks(disks);

        initStorageDomains();
    }

    protected void onInitTemplateDisks(ArrayList<DiskImage> diskImages) {
        for (DiskImage disk : diskImages)
        {
            templateDisks.add(diskImageToModel(disk));
        }
    }

    protected DiskModel diskImageToModel(DiskImage disk) {
        DiskModel diskModel = new DiskModel();
        diskModel.setIsNew(true);
        diskModel.setName(disk.getinternal_drive_mapping());

        EntityModel sizeEntity = new EntityModel();
        sizeEntity.setEntity(disk.getSizeInGigabytes());
        diskModel.setSize(sizeEntity);

        ListModel volumeList = new ListModel();
        volumeList.setItems((disk.getvolume_type() == VolumeType.Preallocated ?
                new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                : DataProvider.GetVolumeTypeList()));
        volumeList.setSelectedItem(disk.getvolume_type());
        diskModel.setVolumeType(volumeList);

        diskModel.setDiskImage(disk);

        return diskModel;
    }

    protected void onInitStorageDomains(ArrayList<storage_domains> storages) {
        for (storage_domains storage : storages) {
            if (Linq.IsDataActiveStorageDomain(storage)) {
                storageDomains.add(storage);
            }
        }

        Collections.sort(storageDomains, new Linq.StorageDomainByNameComparer());

        postInitStorageDomains();
    }

    protected void postInitStorageDomains() {
        activeStorageDomains = getStorageDomains();
        disjointStorageDomains = new ArrayList<storage_domains>();
        unionStorageDomains = new ArrayList<storage_domains>();

        for (DiskModel disk : getDisks()) {
            ArrayList<Guid> diskStorageIds = disk.getDiskImage().getstorage_ids();
            ArrayList<storage_domains> diskStorageDomains =
                    Linq.getStorageDomainsByIds(diskStorageIds, activeStorageDomains);
            disk.getSourceStorageDomain().setItems(diskStorageDomains);

            disjointStorageDomains = Linq.Disjoint(disjointStorageDomains, diskStorageDomains);
            unionStorageDomains = Linq.Union(unionStorageDomains, diskStorageDomains);

            ArrayList<storage_domains> destStorageDomains = Linq.Except(activeStorageDomains, diskStorageDomains);

            if (!disk.getDiskImage().getParentId().equals(NGuid.Empty)) {
                destStorageDomains = filterStorageDomains(destStorageDomains, disk);
            }

            Collections.sort(destStorageDomains, new Linq.StorageDomainByNameComparer());
            disk.getStorageDomain().setItems(destStorageDomains);
        }

        ArrayList<storage_domains> destStorageDomains = Linq.Except(activeStorageDomains, disjointStorageDomains);
        Collections.sort(destStorageDomains, new Linq.StorageDomainByNameComparer());

        getStorageDomain().setItems(destStorageDomains);
        getSourceStorageDomain().setItems(disjointStorageDomains);
        setDisks(getDisks());

        PostCopyOrMoveInit();
    }

    protected ArrayList<storage_domains> filterStorageDomains(ArrayList<storage_domains> storageDomains,
            DiskModel vmdisk) {
        ArrayList<storage_domains> destStorageDomains = new ArrayList<storage_domains>();
        DiskModel templateDisk = getTemplateDiskByVmDisk(vmdisk);

        if (templateDisk != null) {
            for (storage_domains storageDomain : storageDomains) {
                if (templateDisk.getDiskImage().getstorage_ids().contains(storageDomain.getId())) {
                    destStorageDomains.add(storageDomain);
                }
            }
        }

        return destStorageDomains;
    }

    protected DiskModel getTemplateDiskByVmDisk(DiskModel vmdisk) {
        for (DiskModel disk : getTemplateDisks()) {
            if (disk.getDiskImage().getId().equals(vmdisk.getDiskImage().getParentId())) {
                return disk;
            }
        }

        return null;
    }

    private void OnMove() {
        OnMoveOrCopy(ImageOperation.Move);
    }

    private void OnCopy() {
        OnMoveOrCopy(ImageOperation.Copy);
    }

    protected void OnMoveOrCopy(ImageOperation imageOperation) {
        if (this.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (DiskModel diskModel : getDisks())
        {
            storage_domains destStorageDomain = (Boolean) getIsSingleStorageDomain().getEntity() ?
                    (storage_domains) getStorageDomain().getSelectedItem() :
                    (storage_domains) diskModel.getStorageDomain().getSelectedItem();

            storage_domains sourceStorageDomain = (Boolean) getIsSingleStorageDomain().getEntity() ?
                    (storage_domains) getSourceStorageDomain().getSelectedItem() :
                    (storage_domains) diskModel.getSourceStorageDomain().getSelectedItem();

            if (destStorageDomain == null
                    || diskModel.getDiskImage().getstorage_ids().contains(destStorageDomain.getId())) {
                continue;
            }

            Guid sourceStorageDomainGuid = sourceStorageDomain != null ? sourceStorageDomain.getId() : Guid.Empty;
            Guid destStorageDomainGuid = destStorageDomain.getId();
            DiskImage disk = diskModel.getDiskImage();
            MoveOrCopyImageGroupParameters diskParameters =
                    new MoveOrCopyImageGroupParameters(disk.getId(),
                            sourceStorageDomainGuid,
                            destStorageDomainGuid,
                            imageOperation);

            parameters.add(diskParameters);
        }

        StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.MoveOrCopyDisk, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        MoveOrCopyDiskModel localModel = (MoveOrCopyDiskModel) result.getState();
                        localModel.StopProgress();
                        ListModel listModel = (ListModel) localModel.getEntity();
                        listModel.setWindow(null);
                    }
                }, this);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnMove"))
        {
            OnMove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnCopy"))
        {
            OnCopy();
        }
    }
}
