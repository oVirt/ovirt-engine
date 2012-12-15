package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class CopyDiskModel extends MoveOrCopyDiskModel
{
    public CopyDiskModel() {
        super();

        setIsSourceStorageDomainAvailable(true);
        setIsSourceStorageDomainChangable(true);
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        setDiskImages(disksImages);

        AsyncDataProvider.GetDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;

                copyDiskModel.onInitAllDisks(disks);
                copyDiskModel.onInitDisks();
            }
        }));
    }

    @Override
    protected void initStorageDomains() {
        Disk disk = getDisks().get(0).getDisk();
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return;
        }

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                copyDiskModel.onInitStorageDomains(storageDomains);
            }
        }), ((DiskImage) disk).getstorage_pool_id().getValue());
    }

    @Override
    protected void postCopyOrMoveInit() {
        ICommandTarget target = (ICommandTarget) getEntity();

        boolean noSingleStorageDomain = !getStorageDomain().getItems().iterator().hasNext();
        boolean noDestStorageDomain = intersectStorageDomains.containsAll(activeStorageDomains);

        if (activeStorageDomains.isEmpty() || noDestStorageDomain) {
            if (activeStorageDomains.isEmpty()) {
                setMessage(ConstantsManager.getInstance().getConstants().noSDAvailableMsg());
            }
            else if (noDestStorageDomain) {
                setMessage(ConstantsManager.getInstance().getConstants().disksAlreadyExistMsg());
            }

            UICommand tempVar = new UICommand("Cancel", target); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnCopy", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", target); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar3.setIsCancel(true);
            getCommands().add(tempVar3);
        }

        StopProgress();
    }

    @Override
    protected void updateMoveOrCopySingleDiskParameters(ArrayList<VdcActionParametersBase> parameters,
            DiskModel diskModel) {

        ArrayList<storage_domains> selectedStorageDomains = new ArrayList<storage_domains>();
        if (diskModel.getStorageDomain().getSelectedItems() != null) {
            selectedStorageDomains.addAll(diskModel.getStorageDomain().getSelectedItems());
        }
        else {
            selectedStorageDomains.add((storage_domains) diskModel.getStorageDomain().getSelectedItem());
        }

        for (storage_domains storageDomain : selectedStorageDomains) {
            addMoveOrCopyParameters(parameters,
                    Guid.Empty,
                    storageDomain.getId(),
                    (DiskImage) diskModel.getDisk());
        }
    }

    @Override
    protected VdcActionType getActionType() {
        return VdcActionType.MoveOrCopyDisk;
    }

    @Override
    protected MoveOrCopyImageGroupParameters createParameters(Guid sourceStorageDomainGuid,
            Guid destStorageDomainGuid,
            DiskImage disk) {
        return new MoveOrCopyImageGroupParameters(disk.getImageId(),
                sourceStorageDomainGuid,
                destStorageDomainGuid,
                ImageOperation.Copy);
    }

    @Override
    protected void OnExecute() {
        super.OnExecute();

        Frontend.RunMultipleAction(getActionType(), getParameters(),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        CopyDiskModel localModel = (CopyDiskModel) result.getState();
                        localModel.StopProgress();
                        ListModel listModel = (ListModel) localModel.getEntity();
                        listModel.setWindow(null);
                    }
                }, this);
    }

}
