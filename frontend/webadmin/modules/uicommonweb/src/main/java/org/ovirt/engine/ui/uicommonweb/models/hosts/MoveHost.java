package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Extensions;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

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

    private java.util.ArrayList<VDS> privateSelectedHosts;

    public java.util.ArrayList<VDS> getSelectedHosts()
    {
        return privateSelectedHosts;
    }

    public void setSelectedHosts(java.util.ArrayList<VDS> value)
    {
        privateSelectedHosts = value;
    }

    public MoveHost()
    {
        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);
    }

    private void Cluster_SelectedItemChanged()
    {
        if (getCluster().getSelectedItem() != null)
        {
            AsyncDataProvider.GetHostList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            MoveHost moveHost = (MoveHost) target;
                            java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>) returnValue;
                            moveHost.PostGetHostList(hosts);
                        }
                    }));
        }
    }

    private void PostGetHostList(java.util.ArrayList<VDS> hosts) {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) getCluster().getItems();
        java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();

        for (VDS vds : hosts)
        {
            if (Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(vds.getvds_group_id())) == null
                    && (vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval)
                    && (vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion()
                            .getFullVersion()).compareTo(cluster.getcompatibility_version()) >= 0))
            {
                EntityModel entity = new EntityModel();
                entity.setEntity(vds);
                items.add(entity);
            }
        }

        java.util.ArrayList<Guid> previouslySelectedHostIDs = new java.util.ArrayList<Guid>();
        if (getItems() != null)
        {
            for (Object item : getItems())
            {
                EntityModel entity = (EntityModel) item;
                if (entity.getIsSelected())
                {
                    previouslySelectedHostIDs.add(((VDS) entity.getEntity()).getvds_id());
                }
            }
        }
        setItems(items);
        for (EntityModel entity : items)
        {
            entity.setIsSelected(previouslySelectedHostIDs.contains(((VDS) entity.getEntity()).getvds_id()));
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(SelectedItemChangedEventDefinition) && sender == getCluster())
        {
            Cluster_SelectedItemChanged();
        }
    }

    public boolean Validate()
    {
        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getCluster().getIsValid();
    }
}
