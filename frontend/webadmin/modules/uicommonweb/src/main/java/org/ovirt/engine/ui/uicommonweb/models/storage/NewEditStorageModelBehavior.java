package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
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

        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();
        if (dataCenter == null) {
            return;
        }

        // Allow Data storage type corresponding to the selected data-center type + ISO and Export that are NFS only:
        for (IStorageModel item : Linq.<IStorageModel> cast(getModel().getItems()))
        {
            Model model = (Model) item;

            if (item.getRole() == StorageDomainType.ISO)
            {
                AsyncDataProvider.getInstance().getIsoDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
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
                AsyncDataProvider.getInstance().getExportDomainByDataCenterId(new AsyncQuery(new Object[] { this, item },
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

    @Override
    public boolean isImport() {
        return false;
    }

    public void postUpdateItemsAvailability(IStorageModel item, boolean isNoExportOrIsoStorageAttached) {
        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();

        checkCanItemBeSelected(item, dataCenter, isNoExportOrIsoStorageAttached);
    }

    private void checkCanItemBeSelected(final IStorageModel item, StoragePool dataCenter, boolean isNoExportOrIsoStorageAttached) {
        boolean isExistingStorage = getModel().getStorage() != null &&
                item.getType() == getModel().getStorage().getStorageType();

        // If we are in edit mode then the type of the entity edited should appear in the selection
        if (isExistingStorage) {
            updateItemSelectability(item, true);
            return;
        }

        boolean isExportDomain = item.getRole() == StorageDomainType.ImportExport;
        boolean isIsoDomain = item.getRole() == StorageDomainType.ISO;

        // Local types should not be selectable for shared data centers and vice versa, only exception is an
        // export/import and ISO domains which can be added as NFS
        if (!(isExportDomain || isIsoDomain) && isLocalStorage(item) != dataCenter.isLocal()) {
            updateItemSelectability(item, false);
            return;
        }

        boolean isNoneDataCenter = dataCenter.getId().equals(StorageModel.UnassignedDataCenterId);
        boolean isDataDomain = item.getRole() == StorageDomainType.Data;

        // For 'None' data center we allow all data types and no ISO/Export, no reason for further checks
        if (isNoneDataCenter) {
            updateItemSelectability(item, isDataDomain);
            return;
        }

        boolean canAttachExportDomain = isNoExportOrIsoStorageAttached &&
                dataCenter.getStatus() != StoragePoolStatus.Uninitialized;

        boolean canAttachIsoDomain = isNoExportOrIsoStorageAttached &&
                dataCenter.getStatus() != StoragePoolStatus.Uninitialized;

        if ((isExportDomain && canAttachExportDomain) || (isIsoDomain && canAttachIsoDomain)) {
            updateItemSelectability(item, true);
            return;
        }

        if (isDataDomain) {
            if (isLocalStorage(item)) {
                updateItemSelectability(item, true);
                return;
            }

            if (AsyncDataProvider.getInstance().isMixedStorageDomainsSupported(dataCenter.getcompatibility_version())) {
                updateItemSelectability(item, true);
                return;
            } else {
                IdQueryParameters params = new IdQueryParameters(dataCenter.getId());
                Frontend.getInstance().runQuery(VdcQueryType.GetStorageTypesInPoolByPoolId, params,
                                                new AsyncQuery(this, new INewAsyncCallback() {
                                                    @Override
                                                    public void onSuccess(Object model, Object ReturnValue) {
                                                        List<StorageType> storageTypes = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                                        for (StorageType storageType : storageTypes) {
                                                            if (storageType.isBlockDomain() != item.getType().isBlockDomain()) {
                                                                updateItemSelectability(item, false);
                                                                return;
                                                            }
                                                        }
                                                        updateItemSelectability(item, true);
                                                        return;
                                                    }
                                                }));
                return;
            }
        }
        updateItemSelectability(item, false);
    }

    private void updateItemSelectability(IStorageModel item, boolean isSelectable) {
        Model model = (Model) item;
        model.setIsSelectable(isSelectable);
        onStorageModelUpdated(item);
    }
}
