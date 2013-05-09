package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
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
        if (!Frontend.getQueryStartedEvent().getListeners().contains(this))
            Frontend.getQueryStartedEvent().addListener(this);
        if (!Frontend.getQueryCompleteEvent().getListeners().contains(this))
            Frontend.getQueryCompleteEvent().addListener(this);
    }

    public void filterUnSelectableModels()
    {
        // Filter UnSelectable models from AvailableStorageItems list
        ArrayList<Object> filterredItems = new ArrayList<Object>();
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
        getModel().UpdatedStorageModels.add(model);

        // Filter UnSelectable model from AvailableStorageItems list
        if (getModel().UpdatedStorageModels.size() == Linq.<IStorageModel> cast(getModel().getItems()).size())
        {
            getModel().UpdatedStorageModels.clear();

            getModel().getHost().setItems(new ArrayList<HostModel>());
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

        if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            getModel().frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            getModel().frontend_QueryComplete();
        }
    }
}
