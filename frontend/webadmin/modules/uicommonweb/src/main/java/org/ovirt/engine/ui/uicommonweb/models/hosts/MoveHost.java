package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public class MoveHost extends ListModel<EntityModel<VDS>>
{
    private ListModel<VDSGroup> privateCluster;

    public ListModel<VDSGroup> getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel<VDSGroup> value)
    {
        privateCluster = value;
    }

    private ArrayList<VDS> privateSelectedHosts;

    public ArrayList<VDS> getSelectedHosts()
    {
        return privateSelectedHosts;
    }

    public void setSelectedHosts(ArrayList<VDS> value)
    {
        privateSelectedHosts = value;
    }

    private boolean isMultiSelection = true;

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    public void setMultiSelection(boolean isMultiSelection) {
        this.isMultiSelection = isMultiSelection;
    }

    public MoveHost()
    {
        setCluster(new ListModel<VDSGroup>());
        getCluster().getSelectedItemChangedEvent().addListener(this);
    }

    private void cluster_SelectedItemChanged()
    {
        if (getCluster().getSelectedItem() != null)
        {
            AsyncDataProvider.getInstance().getHostList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {

                    ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                    postGetHostList(hosts);
                }
            }));
        }
    }

    private void postGetHostList(ArrayList<VDS> hosts) {

        VDSGroup cluster = getCluster().getSelectedItem();
        ArrayList<EntityModel<VDS>> items = new ArrayList<>();

        for (VDS vds : hosts)
        {
            if (!cluster.getId().equals(vds.getVdsGroupId()) &&
                    (vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                    && vds.getSupportedClusterVersionsSet().contains(cluster.getCompatibilityVersion()))
            {
                EntityModel<VDS> entity = new EntityModel<>();
                entity.setEntity(vds);
                items.add(entity);
            }
        }

        ArrayList<Guid> previouslySelectedHostIDs = new ArrayList<>();
        if (getItems() != null)
        {
            for (EntityModel<VDS> entity : getItems())
            {
                if (entity.getIsSelected())
                {
                    previouslySelectedHostIDs.add(entity.getEntity().getId());
                }
            }
        }
        setItems(items);
        for (EntityModel<VDS> entity : items)
        {
            entity.setIsSelected(previouslySelectedHostIDs.contains((entity.getEntity()).getId()));
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemChangedEventDefinition) && sender == getCluster())
        {
            cluster_SelectedItemChanged();
        }
    }

    public boolean validate()
    {
        getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getCluster().getIsValid();
    }
}
