package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class NewEditStorageModelBehavior extends StorageModelBehavior
{
    @Override
    public void UpdateItemsAvailability()
    {
        super.UpdateItemsAvailability();

        // Allow Data storage type corresponding to the selected data-center type + ISO and Export that are NFS only:
        for (IStorageModel item : Linq.<IStorageModel> Cast(getModel().getItems()))
        {
            Model model = (Model) item;

            storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
            if (dataCenter == null) {
                return;
            }

            if (item.getRole() == StorageDomainType.ISO)
            {
                AsyncDataProvider.GetIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                NewEditStorageModelBehavior behavior = (NewEditStorageModelBehavior) array[0];
                                IStorageModel storageModelItem = (IStorageModel) array[1];
                                behavior.PostUpdateItemsAvailability(behavior, storageModelItem, returnValue == null);

                            }
                        }, getHash()), dataCenter.getId());
            }
            else if (item.getRole() == StorageDomainType.ImportExport)
            {
                AsyncDataProvider.GetExportDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                NewEditStorageModelBehavior behavior = (NewEditStorageModelBehavior) array[0];
                                IStorageModel storageModelItem = (IStorageModel) array[1];
                                behavior.PostUpdateItemsAvailability(behavior, storageModelItem, returnValue == null);

                            }
                        }, getHash()), dataCenter.getId());
            }
            else
            {
                PostUpdateItemsAvailability(this, item, false);
            }
        }
    }

    public void PostUpdateItemsAvailability(NewEditStorageModelBehavior behavior,
            IStorageModel item,
            boolean isNoStorageAttached)
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        Model model = (Model) item;

        model.setIsSelectable(dataCenter != null
                && ((dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) && item.getRole() == StorageDomainType.Data)
                        || (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId) && ((item.getRole() == StorageDomainType.Data && item.getType() == dataCenter.getstorage_pool_type())
                                || (item.getRole() == StorageDomainType.ImportExport
                                        && item.getType() == StorageType.NFS
                                        && dataCenter.getstatus() != StoragePoolStatus.Uninitialized && isNoStorageAttached) || item.getRole() == StorageDomainType.ISO
                                && item.getType() == StorageType.NFS
                                && dataCenter.getstatus() != StoragePoolStatus.Uninitialized && isNoStorageAttached)) || (getModel().getStorage() != null && item.getType() == getModel().getStorage()
                        .getStorageType())));

        behavior.OnStorageModelUpdated(item);
    }
}
