package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class CopyDiskModel extends MoveOrCopyDiskModel
{
    public CopyDiskModel() {
        super();

        setIsSourceStorageDomainAvailable(true);
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        setDiskImages(disksImages);

        AsyncDataProvider.GetDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
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
            public void onSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;

                copyDiskModel.onInitStorageDomains(storageDomains);
            }
        }), ((DiskImage) disk).getStoragePoolId().getValue());
    }

    @Override
    protected VdcActionType getActionType() {
        return VdcActionType.MoveOrCopyDisk;
    }

    @Override
    protected String getWarning() {
        return constants.cannotCopyDisks();
    }

    @Override
    protected String getNoActiveSourceDomainMessage() {
        return constants.noActiveSourceStorageDomainAvailableMsg();
    }

    @Override
    protected String getNoActiveTargetDomainMessage() {
        return constants.diskExistsOnAllActiveStorageDomainsMsg();
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

        ArrayList<VdcActionParametersBase> parameters = getParameters();
        if (parameters.isEmpty()) {
            cancel();
            return;
        }

        Frontend.RunMultipleAction(getActionType(), parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        CopyDiskModel localModel = (CopyDiskModel) result.getState();
                        localModel.cancel();
                    }
                }, this);
    }

}
