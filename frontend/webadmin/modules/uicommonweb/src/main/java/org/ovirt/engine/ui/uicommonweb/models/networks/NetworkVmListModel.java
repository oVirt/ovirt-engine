package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class NetworkVmListModel extends SearchableListModel
{
    private UICommand removeCommand;
    private NetworkVmFilter viewFilterType;

    public NetworkVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public NetworkVmFilter getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(NetworkVmFilter viewFilterType) {
        this.viewFilterType = viewFilterType;
        Search();
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
    }

    public void setEntity(NetworkView value) {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void setItems(Iterable value) {
        if (value != null) {
            List<PairQueryable<VmNetworkInterface, VM>> itemList = (List<PairQueryable<VmNetworkInterface, VM>>) value;
            Collections.sort(itemList, new Comparator<PairQueryable<VmNetworkInterface, VM>>() {

                @Override
                public int compare(PairQueryable<VmNetworkInterface, VM> paramT1,
                        PairQueryable<VmNetworkInterface, VM> paramT2) {
                    int compareValue =
                            paramT1.getSecond().getVdsGroupName().compareTo(paramT2.getSecond().getVdsGroupName());

                    if (compareValue != 0) {
                        return compareValue;
                    }

                    return paramT1.getSecond().getName().compareTo(paramT2.getSecond().getName());
                }
            });
        }
        super.setItems(value);
    }

    @Override
    public void Search() {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch() {
        if (getEntity() == null)
        {
            return;
        }

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(getViewFilterType());
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                if (model.equals(getViewFilterType())) {
                    NetworkVmListModel.this.setItems((List<PairQueryable<VmNetworkInterface, VM>>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                }
            }
        };

        GetVmsAndNetworkInterfacesByNetworkIdParameters params =
                new GetVmsAndNetworkInterfacesByNetworkIdParameters(getEntity().getId(),
                        NetworkVmFilter.running.equals(getViewFilterType()));
        params.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetVmsAndNetworkInterfacesByNetworkId,
                params,
                asyncQuery);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    private void updateActionAvailability() {
        ArrayList<VM> vms = new ArrayList<VM>();
        List<PairQueryable<VmNetworkInterface, VM>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();
        for (PairQueryable<VmNetworkInterface, VM> item : selectedItems)
        {
            vms.add(item.getSecond());
        }

        getRemoveCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(vms,
                VM.class,
                VdcActionType.RemoveVmInterface) && getSelectedItems() != null && !getSelectedItems().isEmpty()
                && canRemoveVnics());
    }

    private boolean canRemoveVnics() {
        List<PairQueryable<VmNetworkInterface, VM>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();
        ArrayList<VmNetworkInterface> nics =
                getSelectedItems() != null ? Linq.<VmNetworkInterface> Cast(getSelectedItems())
                        : new ArrayList<VmNetworkInterface>();

        for (PairQueryable<VmNetworkInterface, VM> pair : selectedItems)
        {
            if (pair.getFirst().isPlugged()
                    && !VMStatus.Down.equals(pair.getSecond().getStatus()))
            {
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
        if (getWindow() != null)
        {
            return;
        }

        List<VmNetworkInterface> vnics = new ArrayList<VmNetworkInterface>();
        for (Object item : getSelectedItems())
        {
            PairQueryable<VmNetworkInterface, VM> pair = (PairQueryable<VmNetworkInterface, VM>) item;
            vnics.add(pair.getFirst());
        }
        RemoveVmInterfaceModel model = new RemoveVmInterfaceModel(this, vnics, true);
        setWindow(model);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getRemoveCommand())
        {
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
