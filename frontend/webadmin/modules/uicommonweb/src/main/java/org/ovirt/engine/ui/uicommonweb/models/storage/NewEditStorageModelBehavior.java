package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;


public class NewEditStorageModelBehavior extends StorageModelBehavior {
    @Override
    public void updateItemsAvailability() {
        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();
        if (dataCenter == null) {
            return;
        }

        // Allow Data storage type corresponding to the selected data-center type + ISO and Export that are NFS only:
        for (final IStorageModel item : getModel().getStorageModels()) {
            if (item.getRole() == StorageDomainType.ISO) {
                AsyncDataProvider.getInstance().getIsoDomainByDataCenterId(new AsyncQuery<>(
                        returnValue -> {

                            IStorageModel storageModelItem = item;
                            postUpdateItemsAvailability(storageModelItem, returnValue == null);

                        }), dataCenter.getId());
            } else if (item.getRole() == StorageDomainType.ImportExport) {
                AsyncDataProvider.getInstance().getExportDomainByDataCenterId(new AsyncQuery<>(
                        returnValue -> {

                            IStorageModel storageModelItem = item;
                            postUpdateItemsAvailability(storageModelItem, returnValue == null);

                        }), dataCenter.getId());
            } else {
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
        boolean isManagedBlockDomain = item.getRole() == StorageDomainType.ManagedBlockStorage;

        // Local types should not be selectable for shared data centers
        if (isLocalStorage(item) && !dataCenter.isLocal()) {
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

        boolean canAttachManagedBlockDomain =
                dataCenter.getStatus() != StoragePoolStatus.Uninitialized && AsyncDataProvider.getInstance()
                        .isManagedBlockDomainSupported(dataCenter.getCompatibilityVersion());

        // local storage should only be available in a local DC.
        boolean canAttachLocalStorage = !isLocalStorage(item) || dataCenter.isLocal();
        if (((isExportDomain && canAttachExportDomain) || (isIsoDomain && canAttachIsoDomain) ||
                (isManagedBlockDomain && canAttachManagedBlockDomain)) && canAttachLocalStorage) {
            updateItemSelectability(item, true);
            return;
        }

        if (isDataDomain) {
            if (isLocalStorage(item)) {
                updateItemSelectability(item, true);
                return;
            }

            updateItemSelectability(item, true);
            return;
        }
        updateItemSelectability(item, false);
    }

    private void updateItemSelectability(IStorageModel item, boolean isSelectable) {
        Model model = (Model) item;
        model.setIsSelectable(isSelectable);
        onStorageModelUpdated(item);
    }
}
