package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class ImportStorageModelBehavior extends StorageModelBehavior
{
    @Override
    public List<StoragePool> filterDataCenter(List<StoragePool> source)
    {
        return Linq.toList(Linq.where(source, new Linq.DataCenterStatusPredicate(StoragePoolStatus.Up)));
    }

    @Override
    public void initialize() {
        super.initialize();
        getModel().getActivateDomain().setEntity(false);
    }

    @Override
    public void updateItemsAvailability()
    {
        super.updateItemsAvailability();

        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();

        updateAvailabilityByDatacenter(dataCenter);

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

    private void updateAvailabilityByDatacenter(StoragePool datacenter) {
        if (datacenter == null) {
            return;
        }

        boolean isUnassignedDC = StorageModel.UnassignedDataCenterId.equals(datacenter.getId());
        Version dcVersion = !isUnassignedDC ? datacenter.getcompatibility_version() : Version.v3_5;
        boolean ovfStoreOnAnyDomainEnabled = (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.ImportDataStorageDomain, dcVersion.getValue());

        if (ovfStoreOnAnyDomainEnabled && !isItemsContainDataDomains()) {
            Collection<IStorageModel> items = new ArrayList<IStorageModel>();
            items.addAll(getModel().getItems());
            items.addAll(AsyncDataProvider.getFileDataStorageModels());
            items.addAll(AsyncDataProvider.getImportBlockDataStorageModels());

            // Set items manually to invoke itemsChanged event
            getModel().setItems(items);
        }

        getModel().getActivateDomain().setIsAvailable(!StorageModel.UnassignedDataCenterId.equals(datacenter.getId()));
    }

    private boolean isItemsContainDataDomains() {
        for (IStorageModel storageModel : getModel().getItems()) {
            if (storageModel.getRole() == StorageDomainType.Data) {
                return true;
            }
        }
        return false;
    }

    public void postUpdateItemsAvailability(IStorageModel item, boolean isNoStorageAttached)
    {
        Model model = (Model) item;
        StoragePool dataCenter = getModel().getDataCenter().getSelectedItem();

        boolean isItemSelectable = isItemSelectable(item, dataCenter, isNoStorageAttached);
        model.setIsSelectable(isItemSelectable);

        onStorageModelUpdated(item);
    }

    @Override
    public void filterUnSelectableModels() {
        super.filterUnSelectableModels();
        if (getModel().getAvailableStorageItems().getItems().isEmpty()) {
            getModel().getDataCenterAlert().setIsAvailable(true);
            getModel().getDataCenterAlert().setEntity(ConstantsManager.getInstance().getConstants().noStoragesToImport());
        }
    }

    @Override
    public boolean isImport() {
        return true;
    }

    private boolean isItemSelectable(IStorageModel item, StoragePool dataCenter, boolean isNoStorageAttached) {
        // Allow import of data domains
        if (item.getRole() == StorageDomainType.Data) {
            return true;
        }

        // Local SD can be attached to a local DC only
        if (isLocalStorage(item) && !dataCenter.isLocal()) {
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
