package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class MigrateModel extends Model {

    private ListModel<VDS> privateHosts;
    private VmListModel<?> parentModel;
    private VM vm;

    public ListModel<VDS> getHosts() {
        return privateHosts;
    }

    private void setHosts(ListModel<VDS> value) {
        privateHosts = value;
    }

    private ListModel<Cluster> clusters;

    public ListModel<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(ListModel<Cluster> clusters) {
        this.clusters = clusters;
    }

    private ArrayList<VM> privateVmList;

    public ArrayList<VM> getVmList() {
        return privateVmList;
    }

    public void setVmList(ArrayList<VM> value) {
        privateVmList = value;
    }

    private boolean privateVmsOnSameCluster;

    public boolean getVmsOnSameCluster() {
        return privateVmsOnSameCluster;
    }

    public void setVmsOnSameCluster(boolean value) {
        privateVmsOnSameCluster = value;
    }

    private boolean isAutoSelect;

    public boolean getIsAutoSelect() {
        return isAutoSelect;
    }

    public void setIsAutoSelect(boolean value) {
        if (isAutoSelect != value) {
            isAutoSelect = value;
            getHosts().setIsChangeable(!isAutoSelect);
            onPropertyChanged(new PropertyChangedEventArgs("IsAutoSelect")); //$NON-NLS-1$
            setIsSameVdsMessageVisible(!value);
            privateSelectHostAutomatically_IsSelected.setEntity(value);
            privateSelectDestinationHost_IsSelected.setEntity(!value);
            privateHosts.setIsChangeable(!value);
        }
    }

    private boolean isHostSelAvailable;

    public boolean getIsHostSelAvailable() {
        return isHostSelAvailable;
    }

    public void setIsHostSelAvailable(boolean value) {
        if (isHostSelAvailable != value) {
            isHostSelAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHostSelAvailable")); //$NON-NLS-1$
        }
    }

    private boolean noSelAvailable;

    public boolean getNoSelAvailable() {
        return noSelAvailable;
    }

    public void setNoSelAvailable(boolean value) {
        if (noSelAvailable != value) {
            noSelAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("NoSelAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isSameVdsMessageVisible;

    public boolean getIsSameVdsMessageVisible() {
        return isSameVdsMessageVisible;
    }

    public void setIsSameVdsMessageVisible(boolean value) {
        isSameVdsMessageVisible = value && gethasSameVdsMessage() && !getIsAutoSelect();
        onPropertyChanged(new PropertyChangedEventArgs("IsSameVdsMessageVisible")); //$NON-NLS-1$
    }

    // onPropertyChanged(new PropertyChangedEventArgs("IsSameVdsMessageVisible"));
    private boolean privatehasSameVdsMessage;

    public boolean gethasSameVdsMessage() {
        return privatehasSameVdsMessage;
    }

    public void sethasSameVdsMessage(boolean value) {
        privatehasSameVdsMessage = value;
    }

    private EntityModel<Boolean> privateSelectHostAutomatically_IsSelected;

    public EntityModel<Boolean> getSelectHostAutomatically_IsSelected() {
        return privateSelectHostAutomatically_IsSelected;
    }

    public void setSelectHostAutomatically_IsSelected(EntityModel<Boolean> value) {
        privateSelectHostAutomatically_IsSelected = value;
    }

    private EntityModel<Boolean> privateSelectDestinationHost_IsSelected;

    public EntityModel<Boolean> getSelectDestinationHost_IsSelected() {
        return privateSelectDestinationHost_IsSelected;
    }

    public void setSelectDestinationHost_IsSelected(EntityModel<Boolean> value) {
        privateSelectDestinationHost_IsSelected = value;
    }

    public MigrateModel(VmListModel<?> parentModel) {
        this.parentModel = parentModel;
        setHosts(new ListModel<VDS>());
        getHosts().getSelectedItemChangedEvent().addListener(this);

        setSelectHostAutomatically_IsSelected(new EntityModel<Boolean>());
        getSelectHostAutomatically_IsSelected().getEntityChangedEvent().addListener(this);

        setSelectDestinationHost_IsSelected(new EntityModel<Boolean>());
        getSelectDestinationHost_IsSelected().getEntityChangedEvent().addListener(this);

        setClusters(new ListModel<Cluster>());
        getClusters().getSelectedItemChangedEvent().addListener(this);
    }

    public void initializeModel() {
        if (vm.getClusterId() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getClusterList(
                new AsyncQuery(MigrateModel.this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        List<Cluster> clusterList = (List<Cluster>) returnValue;
                        List<Cluster> onlyWithArchitecture = AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(clusterList);
                        List<Cluster> onlyVirt = AsyncDataProvider.getInstance().getClusterByServiceList(onlyWithArchitecture, true, false);


                        Cluster selected = null;
                        for (Cluster cluster : onlyVirt) {
                            if (cluster.getId().equals(vm.getClusterId())) {
                                selected = cluster;
                                break;
                            }
                        }

                        clusters.setItems(onlyVirt, selected != null ? selected : Linq.firstOrNull(onlyVirt));
                    }
                }),
                vm.getStoragePoolId());
    }

    private void loadHosts() {
        Cluster selectedCluster = clusters.getSelectedItem();
        if (selectedCluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getUpHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        postMigrateGetUpHosts(privateVmList, (List<VDS>) returnValue);
                    }
                }), selectedCluster.getName());
    }

    private void postMigrateGetUpHosts(List<VM> selectedVms, List<VDS> hosts) {
        setVmsOnSameCluster(true);
        setIsSameVdsMessageVisible(false);
        setNoSelAvailable(false);

        Guid run_on_vds = null;
        boolean allRunOnSameVds = true;

        for (VM item : selectedVms) {
            if (!item.getClusterId().equals((selectedVms.get(0)).getClusterId())) {
                setVmsOnSameCluster(false);
            }
            if (run_on_vds == null) {
                run_on_vds = item.getRunOnVds();
            }
            else if (allRunOnSameVds && !run_on_vds.equals(item.getRunOnVds())) {
                allRunOnSameVds = false;
            }
        }

        setIsHostSelAvailable(getVmsOnSameCluster() && hosts.size() > 0);

        removeUnselectableHosts(hosts, run_on_vds, allRunOnSameVds);

        getCommands().clear();

        if (hosts.isEmpty()) {
            setIsHostSelAvailable(false);
            getHosts().setItems(new ArrayList<VDS>());

            if (allRunOnSameVds) {
                setNoSelAvailable(true);
                UICommand tempVar = new UICommand("Cancel", parentModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                tempVar.setIsDefault(true);
                tempVar.setIsCancel(true);
                getCommands().add(tempVar);
            }
        } else {
            getHosts().setItems(hosts, Linq.firstOrNull(hosts));
            getCommands().add(UICommand.createDefaultOkUiCommand("OnMigrate", parentModel)); //$NON-NLS-1$
            getCommands().add(UICommand.createCancelUiCommand("Cancel", parentModel)); //$NON-NLS-1$
        }
    }

    private void removeUnselectableHosts(List<VDS> hosts, Guid run_on_vds, boolean allRunOnSameVds) {
        if (getVmsOnSameCluster() && allRunOnSameVds) {
            VDS runOnSameVDS = null;
            for (VDS host : hosts) {
                if (host.getId().equals(run_on_vds)) {
                    runOnSameVDS = host;
                }
            }
            hosts.remove(runOnSameVDS);
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
        if (sender == getHosts() && getVmsOnSameCluster()) {
            VDS selectedHost = getHosts().getSelectedItem();
            if (selectedHost == null) {
                return;
            }

            sethasSameVdsMessage(false);
            for (VM vm : getVmList()) {
                if (selectedHost.getId().equals(vm.getRunOnVds())) {
                    sethasSameVdsMessage(true);
                    break;
                }
            }
            setIsSameVdsMessageVisible(gethasSameVdsMessage());
        }
        else if (sender == getClusters()) {
            loadHosts();
        }
        else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getSelectHostAutomatically_IsSelected()) {
                setIsAutoSelect(getSelectHostAutomatically_IsSelected().getEntity());
            }
            else if (sender == getSelectDestinationHost_IsSelected()) {
                setIsAutoSelect(!getSelectDestinationHost_IsSelected().getEntity());
            }
        }
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }
}
