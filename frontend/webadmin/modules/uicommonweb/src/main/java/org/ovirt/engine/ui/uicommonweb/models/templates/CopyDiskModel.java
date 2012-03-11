package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

public class CopyDiskModel extends MoveOrCopyDiskModel
{
    public CopyDiskModel() {
        super();
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        setDiskImages(disksImages);

        AsyncDataProvider.GetDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) returnValue;

                copyDiskModel.onInitAllDisks(diskImages);
                copyDiskModel.onInitDisks();
            }
        }));
    }

    @Override
    protected void initStorageDomains() {
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                copyDiskModel.onInitStorageDomains(storageDomains);
            }
        }), getDisks().get(0).getDiskImage().getstorage_pool_id().getValue());
    }

    protected void postCopyOrMoveInit() {
        ICommandTarget target = (ICommandTarget) getEntity();

        boolean noSingleStorageDomain = !getStorageDomain().getItems().iterator().hasNext();
        boolean noDestStorageDomain = intersectStorageDomains.containsAll(activeStorageDomains);

        if (activeStorageDomains.isEmpty() || noDestStorageDomain) {
            if (activeStorageDomains.isEmpty()) {
                setMessage("No Storage Domain is available - check Storage Domains and Hosts status.");
            }
            else if (noDestStorageDomain) {
                setMessage("Disks already exist on all available Storage Domains.");
            }

            UICommand tempVar = new UICommand("Cancel", target);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnCopy", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", target);
            tempVar3.setTitle("Cancel");
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
            selectedStorageDomains.addAll((ArrayList<storage_domains>) diskModel.getStorageDomain().getSelectedItems());
        }
        else {
            selectedStorageDomains.add((storage_domains) diskModel.getStorageDomain().getSelectedItem());
        }

        for (storage_domains storageDomain : selectedStorageDomains) {
            addMoveOrCopyParameters(parameters,
                    Guid.Empty,
                    storageDomain.getId(),
                    diskModel.getDiskImage(),
                    ImageOperation.Copy);
        }
    }

}
