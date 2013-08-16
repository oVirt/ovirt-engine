package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class ImportStorageModelBehavior extends StorageModelBehavior
{
    @Override
    public List<StoragePool> filterDataCenter(List<StoragePool> source)
    {
        return Linq.toList(Linq.where(source, new Linq.DataCenterStatusPredicate(StoragePoolStatus.Up)));
    }

    @Override
    public void updateItemsAvailability()
    {
        super.updateItemsAvailability();

        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        for (IStorageModel item : Linq.<IStorageModel> cast(getModel().getItems()))
        {
            if (item.getRole() == StorageDomainType.ISO)
            {
                AsyncDataProvider.getIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                ImportStorageModelBehavior behavior = (ImportStorageModelBehavior) array[0];
                                IStorageModel storageModelItem = (IStorageModel) array[1];
                                behavior.postUpdateItemsAvailability(storageModelItem, returnValue == null);

                            }
                        }, getHash()), dataCenter.getId());
            }
            else if (item.getRole() == StorageDomainType.ImportExport)
            {
                AsyncDataProvider.getExportDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                ImportStorageModelBehavior behavior = (ImportStorageModelBehavior) array[0];
                                IStorageModel storageModelItem = (IStorageModel) array[1];
                                behavior.postUpdateItemsAvailability(storageModelItem, returnValue == null);

                            }
                        }, getHash()), dataCenter.getId());
            }
            else
            {
                postUpdateItemsAvailability(item, false);
            }
        }
    }

    public void postUpdateItemsAvailability(IStorageModel item, boolean isNoStorageAttached)
    {
        Model model = (Model) item;
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        boolean isItemSelectable = isItemSelectable(item, dataCenter, isNoStorageAttached);
        model.setIsSelectable(isItemSelectable);

        onStorageModelUpdated(item);
    }

    private boolean isItemSelectable(IStorageModel item, StoragePool dataCenter, boolean isNoStorageAttached) {
        // Local SD can be attached to a local DC only
        if (isLocalStorage(item) && !isLocalDataCenter(dataCenter)) {
            return false;
        }

        // All storage domains can be attached to Unassigned DC
        if (dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
            return true;
        }

        // Local and ISO domains can be attached to DC if it doesn't have
        // an attached domain of the same type already
        if (isNoStorageAttached &&
                (item.getRole() == StorageDomainType.ISO || item.getRole() == StorageDomainType.ImportExport)) {
            return true;
        }

        return false;
    }
}
