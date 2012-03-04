package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;

public class CopyDiskModel extends MoveOrCopyDiskModel
{
    protected VmTemplate template;

    public CopyDiskModel() {
        super();
    }

    public CopyDiskModel(VmTemplate template) {
        super();

        this.template = template;
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        setDiskImages(disksImages);

        onInitDisks();
    }

    @Override
    protected void initStorageDomains() {
        Guid storagePoolId = template.getstorage_pool_id() != null ?
                template.getstorage_pool_id().getValue() : NGuid.Empty;

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                CopyDiskModel copyDiskModel = (CopyDiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                copyDiskModel.onInitStorageDomains(storageDomains);
            }
        }), storagePoolId);
    }

    protected void PostCopyOrMoveInit() {
        ICommandTarget target = (ICommandTarget) getEntity();

        boolean noSingleStorageDomain = !getStorageDomain().getItems().iterator().hasNext();
        boolean noDestStorageDomain =
                activeStorageDomains.isEmpty() || disjointStorageDomains.containsAll(activeStorageDomains);

        if (noSingleStorageDomain || noDestStorageDomain) {
            if (noSingleStorageDomain) {
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

}
