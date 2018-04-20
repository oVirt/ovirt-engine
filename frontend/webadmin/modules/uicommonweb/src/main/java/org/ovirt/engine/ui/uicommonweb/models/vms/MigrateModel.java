package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
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

    public ListModel<VDS> getHosts() {
        return privateHosts;
    }

    private void setHosts(ListModel<VDS> value) {
        privateHosts = value;
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

    private boolean enableSelectionElements;

    public boolean getEnableSelectionElements() {
        return enableSelectionElements;
    }

    public void setEnableSelectionElements(boolean value) {
        enableSelectionElements = value;
        onPropertyChanged(new PropertyChangedEventArgs("EnableSelectionElements")); //$NON-NLS-1$
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
        setEnableSelectionElements(true);

        setSelectHostAutomatically_IsSelected(new EntityModel<Boolean>());
        getSelectHostAutomatically_IsSelected().getEntityChangedEvent().addListener(this);

        setSelectDestinationHost_IsSelected(new EntityModel<Boolean>());
        getSelectDestinationHost_IsSelected().getEntityChangedEvent().addListener(this);
    }

    public void initializeModel() {
        loadHosts();
    }

    private void loadHosts() {
        if (privateVmList == null || privateVmList.isEmpty()) {
            return;
        }

        final Guid clusterId = privateVmList.get(0).getClusterId();
        AsyncDataProvider.getInstance().getValidHostsForVms(new AsyncQuery<>(
                returnValue -> postMigrateGetUpHosts(privateVmList, returnValue)), privateVmList, clusterId);
    }

    private void postMigrateGetUpHosts(List<VM> selectedVms, List<VDS> hosts) {
        setVmsOnSameCluster(true);
        setIsSameVdsMessageVisible(false);
        setNoSelAvailable(false);

        Guid runOnVds = null;
        VM firstSelectedVm = selectedVms.get(0);
        boolean allRunOnSameVds = true;

        for (VM item : selectedVms) {
            if (!item.getClusterId().equals(firstSelectedVm.getClusterId())) {
                setVmsOnSameCluster(false);
            }

            if (runOnVds == null) {
                runOnVds = item.getRunOnVds();
            } else if (allRunOnSameVds && !runOnVds.equals(item.getRunOnVds())) {
                allRunOnSameVds = false;
            }
        }

        setIsHostSelAvailable(getVmsOnSameCluster() && hosts.size() > 0);

        removeUnselectableHosts(hosts, runOnVds, allRunOnSameVds);

        getCommands().clear();

        if (hosts.isEmpty()) {
            setIsHostSelAvailable(false);
            getHosts().setItems(new ArrayList<VDS>());
            setEnableSelectionElements(false);

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

    private void removeUnselectableHosts(List<VDS> hosts,
                                         Guid runOnVds,
                                         boolean allRunOnSameVds) {
        if (getVmsOnSameCluster() && allRunOnSameVds) {
            VDS runOnSameVDS = null;
            for (VDS host : hosts) {
                if (host.getId().equals(runOnVds)) {
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
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getSelectHostAutomatically_IsSelected()) {
                setIsAutoSelect(getSelectHostAutomatically_IsSelected().getEntity());
            } else if (sender == getSelectDestinationHost_IsSelected()) {
                setIsAutoSelect(!getSelectDestinationHost_IsSelected().getEntity());
            }
        }
    }
}
