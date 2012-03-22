package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;

public class MoveDiskModel extends MoveOrCopyDiskModel
{
    protected VM vm;

    public MoveDiskModel() {
        super();
    }

    public MoveDiskModel(VM vm) {
        super();

        this.vm = vm;
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        setDiskImages(disksImages);

        if (vm != null && !vm.getvmt_guid().equals(NGuid.Empty)) {
            AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                    ArrayList<DiskImage> disksImages = (ArrayList<DiskImage>) returnValue;

                    moveDiskModel.onInitTemplateDisks(disksImages);
                    moveDiskModel.onInitDisks();
                }
            }), vm.getvmt_guid());
        }
        else {
            onInitDisks();
        }
    }

    @Override
    protected void initStorageDomains() {
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                moveDiskModel.onInitStorageDomains(storageDomains);
            }
        }), vm.getstorage_pool_id());
    }

    protected void postCopyOrMoveInit() {
        ICommandTarget target = (ICommandTarget) getEntity();

        if (!getStorageDomain().getItems().iterator().hasNext())
        {
            setMessage("The system could not find available target Storage Domain.\nPossible reasons:\n  - No active Storage Domain available\n  - The Template that the VM is based on does not exist on active Storage Domain");

            UICommand tempVar = new UICommand("Cancel", target);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnMove", this);
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

        storage_domains selectedStorageDomain = (storage_domains) diskModel.getStorageDomain().getSelectedItem();

        addMoveOrCopyParameters(parameters,
                Guid.Empty,
                selectedStorageDomain.getId(),
                diskModel.getDiskImage(),
                ImageOperation.Move);
    }

}
