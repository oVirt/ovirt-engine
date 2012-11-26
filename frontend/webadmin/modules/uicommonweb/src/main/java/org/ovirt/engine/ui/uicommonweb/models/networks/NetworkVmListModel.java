package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NetworkVmListModel extends SearchableListModel
{
    private UICommand removeCommand;

    public NetworkVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
    }

    public void setEntity(NetworkView value) {
        super.setEntity(value);
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
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

                    return paramT1.getSecond().getVmName().compareTo(paramT2.getSecond().getVmName());
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
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                NetworkVmListModel.this.setItems((List<PairQueryable<VmNetworkInterface, VM>>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        NetworkIdParameters networkIdParams = new NetworkIdParameters(getEntity().getNetwork().getId());
        networkIdParams.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetVmsAndNetworkInterfacesByNetworkId, networkIdParams, asyncQuery);
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && !getSelectedItems().isEmpty()
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
            Boolean isActivateSupported =
                    (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.HotPlugEnabled,
                            pair.getSecond().getVdsGroupCompatibilityVersion().toString());
            isActivateSupported = isActivateSupported != null ? isActivateSupported : false;

            // If the vm is up and the vnic is active- remove enabled just if hotplug is enabled on the cluster's
            // version
            if (!isActivateSupported && pair.getFirst().isActive()
                    && !VMStatus.Down.equals(pair.getSecond().getStatus()))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged() {
        super.SelectedItemsChanged();
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
