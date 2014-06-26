package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

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

    private String privateHash;

    public String getHash()
    {
        return privateHash;
    }

    public void setHash(String value)
    {
        privateHash = value;
    }

    public List<StoragePool> filterDataCenter(List<StoragePool> source)
    {
        return Linq.toList(Linq.where(source, new Linq.DataCenterNotStatusPredicate(StoragePoolStatus.NotOperational)));
    }

    public void updateItemsAvailability()
    {
        if (!Frontend.getInstance().getQueryStartedEvent().getListeners().contains(this)) {
            Frontend.getInstance().getQueryStartedEvent().addListener(this);
        }
        if (!Frontend.getInstance().getQueryCompleteEvent().getListeners().contains(this)) {
            Frontend.getInstance().getQueryCompleteEvent().addListener(this);
        }
    }

    public void filterUnSelectableModels()
    {
        // Filter UnSelectable models from AvailableStorageItems list
        ArrayList<IStorageModel> filterredItems = new ArrayList<IStorageModel>();
        ArrayList<IStorageModel> items = Linq.<IStorageModel> cast(getModel().getItems());
        for (IStorageModel model : items)
        {
            if (((Model) model).getIsSelectable())
            {
                filterredItems.add(model);
            }
        }

        getModel().getAvailableStorageItems().setItems(filterredItems);
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

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(Frontend.getInstance().getQueryStartedEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            getModel().frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryCompleteEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            getModel().frontend_QueryComplete();
        }
    }

    protected boolean isLocalStorage(IStorageModel storage) {
        return storage.getType() == StorageType.LOCALFS;
    }

    public abstract boolean isImport();
}
