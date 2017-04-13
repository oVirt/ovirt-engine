package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public class MoveHost extends ListModel<MoveHostData> {
    private ListModel<Cluster> privateCluster;

    public ListModel<Cluster> getCluster() {
        return privateCluster;
    }

    private void setCluster(ListModel<Cluster> value) {
        privateCluster = value;
    }

    private ArrayList<MoveHostData> privateSelectedHosts;

    public ArrayList<MoveHostData> getSelectedHosts() {
        return privateSelectedHosts;
    }

    public void setSelectedHosts(ArrayList<MoveHostData> value) {
        privateSelectedHosts = value;
    }

    private boolean isMultiSelection = true;

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    public void setMultiSelection(boolean isMultiSelection) {
        this.isMultiSelection = isMultiSelection;
    }

    public MoveHost() {
        setCluster(new ListModel<Cluster>());
        getCluster().getSelectedItemChangedEvent().addListener(this);
    }

    private void cluster_SelectedItemChanged() {
        if (getCluster().getSelectedItem() != null) {
            AsyncDataProvider.getInstance().getHostList(new AsyncQuery<>(hosts -> postGetHostList(hosts)));
        }
    }

    private void postGetHostList(List<VDS> hosts) {

        Cluster cluster = getCluster().getSelectedItem();
        ArrayList<MoveHostData> items = new ArrayList<>();

        for (VDS vds : hosts) {
            if (!cluster.getId().equals(vds.getClusterId()) &&
                    (vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                    && vds.getSupportedClusterVersionsSet().contains(cluster.getCompatibilityVersion())) {
                MoveHostData entity = new MoveHostData(vds);
                entity.setActivateHost(true);
                items.add(entity);
            }
        }

        ArrayList<Guid> previouslySelectedHostIDs = new ArrayList<>();
        if (getItems() != null) {
            for (MoveHostData entity : getItems()) {
                if (entity.getIsSelected()) {
                    previouslySelectedHostIDs.add(entity.getEntity().getId());
                }
            }
        }
        setItems(items);
        for (MoveHostData entity : items) {
            entity.setIsSelected(previouslySelectedHostIDs.contains(entity.getEntity().getId()));
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemChangedEventDefinition) && sender == getCluster()) {
            cluster_SelectedItemChanged();
        }
    }

    public boolean validate() {
        getCluster().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getCluster().getIsValid();
    }
}
