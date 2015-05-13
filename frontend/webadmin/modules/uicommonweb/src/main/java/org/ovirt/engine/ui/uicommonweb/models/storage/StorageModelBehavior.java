package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public abstract class StorageModelBehavior extends Model
{
    private StorageModel privateModel;

    public StorageModel getModel()
    {
        return privateModel;
    }

    public void setModel(StorageModel value)
    {
        privateModel = value;
    }

    public List<StoragePool> filterDataCenter(List<StoragePool> source)
    {
        return Linq.toList(Linq.where(source, new Linq.DataCenterNotStatusPredicate(StoragePoolStatus.NotOperational)));
    }

    public abstract void updateItemsAvailability();

    public void filterUnSelectableModels()
    {
        // Filter UnSelectable models from AvailableStorageItems list
        ArrayList<IStorageModel> items = Linq.<IStorageModel> cast(getModel().getItems());
        Set<StorageDomainType> storageDomainTypeItems = new LinkedHashSet<StorageDomainType>();
        Set<StorageType> storageTypeItems = new LinkedHashSet<StorageType>();

        // This is needed as long the AvailableStorageItems List is in use. Other code parts rely on this information. See ImportStorageModelBehavior.
        ArrayList<IStorageModel> filterredItems = new ArrayList<IStorageModel>();
        for (IStorageModel model : items)
        {
            if (((Model) model).getIsSelectable())
            {
                filterredItems.add(model);
                storageDomainTypeItems.add(model.getRole());
                storageTypeItems.add(model.getType());
            }
        }
        getModel().getAvailableStorageItems().setItems(filterredItems);
        getModel().getAvailableStorageDomainTypeItems().setItems(storageDomainTypeItems);
        getModel().getAvailableStorageTypeItems().setItems(storageTypeItems);
    }

    public void onStorageModelUpdated(IStorageModel model)
    {
        // Update models list (the list is used for checking update completion)
        getModel().updatedStorageModels.add(model);

        // Filter UnSelectable model from AvailableStorageItems list
        if (getModel().updatedStorageModels.size() == Linq.<IStorageModel> cast(getModel().getItems()).size())
        {
            getModel().updatedStorageModels.clear();

            getModel().getHost().setItems(new ArrayList<VDS>());
            getModel().getHost().setSelectedItem(null);

            filterUnSelectableModels();

            if (getModel().getSelectedItem() != null) {
                getModel().updateFormat();
            }
        }
    }

    public final void updateDataCenterAlert() {
        StoragePool selectedItem = getModel().getDataCenter().getSelectedItem();
        EntityModel alert = getModel().getDataCenterAlert();
        if (shouldShowDataCenterAlert(selectedItem)) {
            alert.setIsAvailable(true);
            alert.setEntity(getDataCenterAlertMessage());
        }
        else {
            alert.setIsAvailable(false);
            alert.setEntity("");
        }
    }

    public abstract boolean shouldShowDataCenterAlert(StoragePool selectedDataCenter);

    public abstract String getDataCenterAlertMessage();

    protected boolean isLocalStorage(IStorageModel storage) {
        return storage.getType() == StorageType.LOCALFS;
    }

    public abstract boolean isImport();
}
