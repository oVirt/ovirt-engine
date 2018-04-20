package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ImportStorageModelBehavior extends StorageModelBehavior {
    @Override
    public List<StoragePool> filterDataCenter(List<StoragePool> source) {
        return Linq.where(source, new Linq.StatusPredicate<>(StoragePoolStatus.Up));
    }

    @Override
    public void initialize() {
        super.initialize();
        getModel().getActivateDomain().setEntity(false);
        initializeItems();
    }

    private void initializeItems() {
        List<IStorageModel> items = AsyncDataProvider.getInstance().getFileDataStorageModels();
        items.addAll(AsyncDataProvider.getInstance().getImportBlockDataStorageModels());
        items.addAll(AsyncDataProvider.getInstance().getIsoStorageModels());
        items.addAll(AsyncDataProvider.getInstance().getExportStorageModels());

        getModel().setStorageModels(items);
    }

    @Override
    public void updateItemsAvailability() {
        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();

        updateAvailabilityByDatacenter(dataCenter);

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

    private void updateAvailabilityByDatacenter(StoragePool datacenter) {
        getModel().getActivateDomain().setIsAvailable(!StorageModel.UnassignedDataCenterId.equals(datacenter.getId()));
    }

    public void postUpdateItemsAvailability(IStorageModel item, boolean isNoStorageAttached) {
        Model model = (Model) item;
        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();

        boolean isItemSelectable = isItemSelectable(item, dataCenter, isNoStorageAttached);
        model.setIsSelectable(isItemSelectable);

        onStorageModelUpdated(item);
    }

    @Override
    public void setStorageTypeItems() {
        super.setStorageTypeItems();
        if (getSelectableModels().isEmpty()) {
            getModel().getDataCenterAlert().setIsAvailable(true);
            getModel().getDataCenterAlert().setEntity(ConstantsManager.getInstance().getConstants().noStoragesToImport());
        }
    }

    @Override
    public boolean isImport() {
        return true;
    }

    private boolean isItemSelectable(IStorageModel item, StoragePool dataCenter, boolean isNoStorageAttached) {

        // Local SD can be attached to a local DC only
        if (isLocalStorage(item) && !dataCenter.isLocal()) {
            return false;
        }

        // All storage domains can be attached to Unassigned DC
        if (StorageModel.UnassignedDataCenterId.equals(dataCenter.getId())) {
            return true;
        }

        // Local and ISO domains can be attached to DC if it doesn't have
        // an attached domain of the same type already
        if (isNoStorageAttached &&
                (item.getRole() == StorageDomainType.ISO || item.getRole() == StorageDomainType.ImportExport)) {
            return true;
        }

        if (item.getRole() == StorageDomainType.Data) {
            return true;
        }

        return false;
    }
}
