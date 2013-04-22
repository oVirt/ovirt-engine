package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class ImportStorageModelBehavior extends StorageModelBehavior
{
    @Override
    public List<storage_pool> FilterDataCenter(List<storage_pool> source)
    {
        return Linq.ToList(Linq.Where(source, new Linq.DataCenterStatusPredicate(StoragePoolStatus.Up)));
    }

    @Override
    public void UpdateItemsAvailability()
    {
        super.UpdateItemsAvailability();

        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        for (IStorageModel item : Linq.<IStorageModel> Cast(getModel().getItems()))
        {
            if (item.getRole() == StorageDomainType.ISO)
            {
                AsyncDataProvider.GetIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                ImportStorageModelBehavior behavior = (ImportStorageModelBehavior) array[0];
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
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                ImportStorageModelBehavior behavior = (ImportStorageModelBehavior) array[0];
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

    public void PostUpdateItemsAvailability(ImportStorageModelBehavior behavior,
            IStorageModel item,
            boolean isNoStorageAttached)
    {
        Model model = (Model) item;
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        // available type/function items are:
        // all in case of Unassigned DC.
        // ISO in case the specified DC doesn't have an attached ISO domain.
        // Export in case the specified DC doesn't have an attached export domain.
        model.setIsSelectable((dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)
                || (item.getRole() == StorageDomainType.ISO && isNoStorageAttached) || (item.getRole() == StorageDomainType.ImportExport && isNoStorageAttached)));

        behavior.OnStorageModelUpdated(item);
    }
}
