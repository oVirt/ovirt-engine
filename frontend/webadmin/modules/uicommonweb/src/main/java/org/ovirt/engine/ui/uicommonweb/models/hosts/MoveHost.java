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
public class MoveHost extends ListModel
{
    private ListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel value)
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
        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);
    }

    private void cluster_SelectedItemChanged()
    {
        if (getCluster().getSelectedItem() != null)
        {
            AsyncDataProvider.getInstance().getHostList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {

                    MoveHost moveHost = (MoveHost) target;
                    ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                    moveHost.postGetHostList(hosts);
                }
            }));
        }
    }

    private void postGetHostList(ArrayList<VDS> hosts) {

        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        ArrayList<EntityModel> items = new ArrayList<EntityModel>();

        for (VDS vds : hosts)
        {
            if (!cluster.getId().equals(vds.getVdsGroupId()) &&
                    (vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                    && vds.getSupportedClusterVersionsSet().contains(cluster.getcompatibility_version()))
            {
                EntityModel entity = new EntityModel();
                entity.setEntity(vds);
                items.add(entity);
            }
        }

        ArrayList<Guid> previouslySelectedHostIDs = new ArrayList<Guid>();
        if (getItems() != null)
        {
            for (Object item : getItems())
            {
                EntityModel entity = (EntityModel) item;
                if (entity.getIsSelected())
                {
                    previouslySelectedHostIDs.add(((VDS) entity.getEntity()).getId());
                }
            }
        }
        setItems(items);
        for (EntityModel entity : items)
        {
            entity.setIsSelected(previouslySelectedHostIDs.contains(((VDS) entity.getEntity()).getId()));
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
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
