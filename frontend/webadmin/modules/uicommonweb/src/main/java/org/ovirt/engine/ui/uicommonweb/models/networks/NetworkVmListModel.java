package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkVmListModel extends SearchableListModel<NetworkView, PairQueryable<VmNetworkInterface, VM>> {
    private UICommand removeCommand;
    private NetworkVmFilter viewFilterType;

    public NetworkVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setHashName("virtual_machines"); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);

        setComparator(new Comparator<PairQueryable<VmNetworkInterface, VM>>() {

            @Override
            public int compare(PairQueryable<VmNetworkInterface, VM> paramT1,
                    PairQueryable<VmNetworkInterface, VM> paramT2) {
                int compareValue =
                        paramT1.getSecond().getClusterName().compareTo(paramT2.getSecond().getClusterName());

                if (compareValue != 0) {
                    return compareValue;
                }

                return paramT1.getSecond().getName().compareTo(paramT2.getSecond().getName());
            }
        });

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public NetworkVmFilter getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(NetworkVmFilter viewFilterType) {
        this.viewFilterType = viewFilterType;
        search();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(getViewFilterType());
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                if (model.equals(getViewFilterType())) {
                    setItems((Collection<PairQueryable<VmNetworkInterface, VM>>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                }
            }
        };

        GetVmsAndNetworkInterfacesByNetworkIdParameters params =
                new GetVmsAndNetworkInterfacesByNetworkIdParameters(getEntity().getId(),
                        NetworkVmFilter.running.equals(getViewFilterType()));
        params.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetVmsAndNetworkInterfacesByNetworkId,
                params,
                asyncQuery);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$ {
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        ArrayList<VM> vms = new ArrayList<>();
        Iterable<PairQueryable<VmNetworkInterface, VM>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();
        for (PairQueryable<VmNetworkInterface, VM> item : selectedItems) {
            vms.add(item.getSecond());
        }

        getRemoveCommand().setIsExecutionAllowed(VdcActionUtils.canExecute(vms,
                VM.class,
                VdcActionType.RemoveVmInterface) && getSelectedItems() != null && !getSelectedItems().isEmpty()
                && canRemoveVnics());
    }

    private boolean canRemoveVnics() {
        Iterable<PairQueryable<VmNetworkInterface, VM>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();

        for (PairQueryable<VmNetworkInterface, VM> pair : selectedItems) {
            if (pair.getFirst().isPlugged()
                    && !VMStatus.Down.equals(pair.getSecond().getStatus())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        List<VmNetworkInterface> vnics = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            PairQueryable<VmNetworkInterface, VM> pair = (PairQueryable<VmNetworkInterface, VM>) item;
            vnics.add(pair.getFirst());
        }
        RemoveVmInterfaceModel model = new RemoveVmInterfaceModel(this, vnics, true);
        setWindow(model);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        }
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    @Override
    protected String getListName() {
        return "NetworkVmListModel"; //$NON-NLS-1$
    }
}
