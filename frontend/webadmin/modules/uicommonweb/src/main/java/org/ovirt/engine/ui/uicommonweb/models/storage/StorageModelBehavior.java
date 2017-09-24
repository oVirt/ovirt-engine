package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public abstract class StorageModelBehavior extends Model {
    private StorageModel model;

    public StorageModel getModel() {
        return model;
    }

    public void setModel(StorageModel value) {
        model = value;
    }

    public List<StoragePool> filterDataCenter(List<StoragePool> source) {
        return Linq.where(source, new Linq.StatusPredicate<>(StoragePoolStatus.NotOperational).negate());
    }

    public abstract void updateItemsAvailability();

    public void setStorageTypeItems() {
        ArrayList<IStorageModel> filteredItems = getSelectableModelsByRole();
        Set<StorageType> storageTypeItems = new LinkedHashSet<>();

        for (IStorageModel model : filteredItems) {
            storageTypeItems.add(model.getType());
        }
        getModel().getAvailableStorageTypeItems().setItems(storageTypeItems);
        getModel().storageItemsChanged();
    }

    public void setStorageDomainTypeItems() {
        ArrayList<IStorageModel> filteredItems = getSelectableModels();
        Set<StorageDomainType> storageDomainTypeItems = new LinkedHashSet<>();

        for (IStorageModel model : filteredItems) {
            storageDomainTypeItems.add(model.getRole());
        }
        getModel().getAvailableStorageDomainTypeItems().setItems(storageDomainTypeItems);
    }

    public ArrayList<IStorageModel> getSelectableModels() {
        // Filter un-selectable models
        List<IStorageModel> items = getModel().getStorageModels();
        return getSelectableModels(items);
    }

    public ArrayList<IStorageModel> getSelectableModelsByRole() {
        StorageDomainType role = getModel().getAvailableStorageDomainTypeItems().getSelectedItem();
        List<IStorageModel> items = getModel().getStorageModelsByRole(role);
        return getSelectableModels(items);
    }

    public ArrayList<IStorageModel> getSelectableModels(List<IStorageModel> storageItems) {
        // Filter un-selectable models
        ArrayList<IStorageModel> filteredItems = new ArrayList<>();
        for (IStorageModel model : storageItems) {
            if (((Model) model).getIsSelectable()) {
                filteredItems.add(model);
            }
        }
        return filteredItems;
    }

    public void onStorageModelUpdated(IStorageModel model) {
        // Update models list (the list is used for checking update completion)
        getModel().updatedStorageModels.add(model);

        // Filter UnSelectable model from AvailableStorageItems list
        if (getModel().updatedStorageModels.size() == getModel().getStorageModels().size()) {
            getModel().updatedStorageModels.clear();

            getModel().getHost().setItems(new ArrayList<>());
            getModel().getHost().setSelectedItem(null);

            setStorageTypeItems();
            setStorageDomainTypeItems();

            if (getModel().getCurrentStorageItem() != null) {
                getModel().updateFormat();
            }
        }
    }

    public final void updateDataCenterAlert() {
        EntityModel<String> alert = getModel().getDataCenterAlert();
        alert.setIsAvailable(false);
        alert.setEntity("");
    }

    protected boolean isLocalStorage(IStorageModel storage) {
        return storage.getType() == StorageType.LOCALFS;
    }

    public abstract boolean isImport();
}
