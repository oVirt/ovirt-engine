package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class MoveDiskModel extends MoveOrCopyDiskModel
{
    public MoveDiskModel() {
        super();
    }

    @Override
    public void init(ArrayList<DiskImage> diskImages) {
        setDiskImages(diskImages);

        AsyncDataProvider.GetDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                ArrayList<Disk> diskImages = (ArrayList<Disk>) returnValue;

                moveDiskModel.onInitAllDisks(diskImages);
                moveDiskModel.onInitDisks();
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
                MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;
                moveDiskModel.onInitStorageDomains(storageDomains);
            }
        }), ((DiskImage) disk).getstorage_pool_id().getValue());
    }

    @Override
    protected void postCopyOrMoveInit() {
        ICommandTarget target = (ICommandTarget) getEntity();

        if (!getStorageDomain().getItems().iterator().hasNext())
        {
            setWarningAvailable(false);
            setMessage(ConstantsManager.getInstance().getConstants()
                    .theSystemCouldNotFindAvailableTargetStorageDomainMsg());

            UICommand tempVar = new UICommand("Cancel", target); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnMove", this); //$NON-NLS-1$
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

        storage_domains selectedStorageDomain = (storage_domains) diskModel.getStorageDomain().getSelectedItem();

        addMoveOrCopyParameters(parameters,
                Guid.Empty,
                selectedStorageDomain.getId(),
                (DiskImage) diskModel.getDisk());
    }

    @Override
    protected VdcActionType getActionType() {
        return VdcActionType.MoveDisks;
    }

    @Override
    protected MoveOrCopyImageGroupParameters createParameters(Guid sourceStorageDomainGuid,
            Guid destStorageDomainGuid,
            DiskImage disk) {
        return new MoveDiskParameters(disk.getImageId(),
                sourceStorageDomainGuid,
                destStorageDomainGuid);
    }

    @Override
    protected void OnExecute() {
        super.OnExecute();

        MoveDisksParameters parameters = new MoveDisksParameters((List) getParameters());
        Frontend.RunAction(getActionType(), parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                        MoveDiskModel localModel = (MoveDiskModel) result.getState();
                        localModel.StopProgress();
                        ListModel listModel = (ListModel) localModel.getEntity();
                        listModel.setWindow(null);
                    }
                }, this);
    }
}
