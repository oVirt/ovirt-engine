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
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class MoveDiskModel extends MoveOrCopyDiskModel
{
    public MoveDiskModel() {
        super();

        setIsSourceStorageDomainNameAvailable(true);
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
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                moveDiskModel.onInitStorageDomains(storageDomains);
            }
        }), ((DiskImage) disk).getStoragePoolId().getValue());
    }

    @Override
    protected VdcActionType getActionType() {
        return VdcActionType.MoveDisks;
    }

    @Override
    protected String getWarning() {
        return constants.cannotMoveDisks();
    }

    @Override
    protected String getNoActiveSourceDomainMessage() {
        return constants.sourceStorageDomainIsNotActiveMsg();
    }

    @Override
    protected String getNoActiveTargetDomainMessage() {
        return constants.noActiveTargetStorageDomainAvailableMsg();
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

        ArrayList<VdcActionParametersBase> parameters = getParameters();
        if (parameters.isEmpty()) {
            cancel();
            return;
        }

        MoveDisksParameters moveDisksParameters = new MoveDisksParameters((List) parameters);
        Frontend.RunAction(getActionType(), moveDisksParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                        MoveDiskModel localModel = (MoveDiskModel) result.getState();
                        localModel.cancel();
                    }
                }, this);
    }
}
