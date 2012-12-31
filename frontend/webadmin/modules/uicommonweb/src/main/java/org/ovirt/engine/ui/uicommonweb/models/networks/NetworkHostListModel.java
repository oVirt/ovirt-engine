package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NetworkHostListModel extends SearchableListModel
{
    private UICommand setupNetworksCommand;
    private NetworkHostFilter viewFilterType;

    public NetworkHostListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
        setHashName("hosts"); //$NON-NLS-1$

        setSetupNetworksCommand(new UICommand("SetupNetworks", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        getSearchCommand().Execute();
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
            public void OnSuccess(Object model, Object ReturnValue)
            {
                if (model.equals(getViewFilterType())) {
                    Iterable returnList = (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                    List<PairQueryable<VdsNetworkInterface, VDS>> items =
                            new ArrayList<PairQueryable<VdsNetworkInterface, VDS>>();
                    for (Object obj : returnList) {
                        if (obj instanceof VDS) {
                            items.add(new PairQueryable<VdsNetworkInterface, VDS>(null, (VDS) obj));
                        } else {
                            items.add((PairQueryable<VdsNetworkInterface, VDS>) obj);
                        }
                    }

                    NetworkHostListModel.this.setItems(items);
                }
            }
        };

        NetworkIdParameters networkIdParams = new NetworkIdParameters(getEntity().getId());
        networkIdParams.setRefresh(getIsQueryFirstTime());

        if (NetworkHostFilter.unattached.equals(getViewFilterType())) {
            Frontend.RunQuery(VdcQueryType.GetVdsWithoutNetwork, networkIdParams, asyncQuery);
        } else if (NetworkHostFilter.attached.equals(getViewFilterType())) {
            Frontend.RunQuery(VdcQueryType.GetVdsAndNetworkInterfacesByNetworkId, networkIdParams, asyncQuery);
        }

        setIsQueryFirstTime(false);
    }

    @Override
    public void setItems(Iterable value) {
        Collections.sort((List<PairQueryable<VdsNetworkInterface, VDS>>) value,
                new Comparator<PairQueryable<VdsNetworkInterface, VDS>>() {

                    @Override
                    public int compare(PairQueryable<VdsNetworkInterface, VDS> arg0,
                            PairQueryable<VdsNetworkInterface, VDS> arg1) {
                        int compareValue =
                                arg0.getSecond().getvds_group_name().compareTo(arg1.getSecond().getvds_group_name());

                        if (compareValue != 0) {
                            return compareValue;
                        }

                        return arg0.getSecond().getvds_name().compareTo(arg1.getSecond().getvds_name());
                    }
                });
        super.setItems(value);
    }

    public void setupNetworks() {
        if (getWindow() != null) {
            return;
        }

        HostSetupNetworksModel setupNetworksWindowModel = new HostSetupNetworksModel(this);

        // set entity
        setupNetworksWindowModel.setEntity(((PairQueryable<VdsNetworkInterface, VDS>) getSelectedItem()).getSecond());

        setWindow(setupNetworksWindowModel);
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getSetupNetworksCommand())
        {
            setupNetworks();
        }

    }

    private void updateActionAvailability() {
        List<PairQueryable<VdsNetworkInterface, VDS>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();

        getSetupNetworksCommand().setIsExecutionAllowed(selectedItems.size() == 1
                && selectedItems.get(0).getSecond().getvds_group_compatibility_version().compareTo(Version.v3_1) >= 0);
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

    public UICommand getSetupNetworksCommand() {
        return setupNetworksCommand;
    }

    private void setSetupNetworksCommand(UICommand value) {
        setupNetworksCommand = value;
    }

    public NetworkHostFilter getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(NetworkHostFilter viewFilterType) {
        this.viewFilterType = viewFilterType;
        Search();
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
    }

    public void setEntity(NetworkView value)
    {
        super.setEntity(value);
    }

    @Override
    protected String getListName() {
        return "NetworkHostListModel"; //$NON-NLS-1$
    }
}
