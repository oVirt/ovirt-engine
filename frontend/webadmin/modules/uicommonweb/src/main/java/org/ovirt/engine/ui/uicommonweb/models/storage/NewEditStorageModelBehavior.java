package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class NewEditStorageModelBehavior extends StorageModelBehavior
{
    @Override
    public void updateItemsAvailability()
    {
        super.updateItemsAvailability();

        // Allow Data storage type corresponding to the selected data-center type + ISO and Export that are NFS only:
        for (IStorageModel item : Linq.<IStorageModel> cast(getModel().getItems()))
        {
            Model model = (Model) item;

            StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();
            if (dataCenter == null) {
                return;
            }

            if (item.getRole() == StorageDomainType.ISO)
            {
                AsyncDataProvider.getIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                NewEditStorageModelBehavior behavior = (NewEditStorageModelBehavior) array[0];
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
                                NewEditStorageModelBehavior behavior = (NewEditStorageModelBehavior) array[0];
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

    public void postUpdateItemsAvailability(IStorageModel item, boolean isNoExportOrIsoStorageAttached) {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();
        Model model = (Model) item;

        boolean isExistingStorage = getModel().getStorage() != null &&
                item.getType() == getModel().getStorage().getStorageType();
        boolean isNoneDataCenter = dataCenter != null &&
                dataCenter.getId().equals(StorageModel.UnassignedDataCenterId);

        boolean isData = item.getRole() == StorageDomainType.Data;
        boolean isExportOrIso = item.getRole() == StorageDomainType.ImportExport || item.getRole() == StorageDomainType.ISO;

        boolean canAttachData = isData && item.getType() == dataCenter.getStorageType();
        boolean canAttachExportOrIso = isExportOrIso && isNoExportOrIsoStorageAttached &&
                dataCenter.getstatus() != StoragePoolStatus.Uninitialized;

        model.setIsSelectable(isExistingStorage || (isNoneDataCenter && isData) ||
                (!isNoneDataCenter && (canAttachData || canAttachExportOrIso)));

        onStorageModelUpdated(item);
    }
}
