package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkHostListModel extends SearchableListModel<NetworkView, PairQueryable<VdsNetworkInterface, VDS>> {
    private UICommand setupNetworksCommand;
    private NetworkHostFilter viewFilterType;

    private Collection<VdsNetworkInterface> attachedByLabelInterfaces;

    public NetworkHostListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
        setHelpTag(HelpTag.hosts);
        setHashName("hosts"); //$NON-NLS-1$

        setComparator(
                Comparator.comparing((PairQueryable<VdsNetworkInterface, VDS> p) -> p.getSecond()
                        .getClusterName()).thenComparing(p -> p.getSecond().getName()));

        setSetupNetworksCommand(new UICommand("SetupNetworks", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    public void setItems(Collection<PairQueryable<VdsNetworkInterface, VDS>> value) {
        AsyncDataProvider.getInstance().updateVDSDefaultRouteRole(value, () -> super.setItems(value));
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

        final NetworkHostFilter filter = getViewFilterType();
        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            if (filter.equals(getViewFilterType())) {
                final Iterable returnList = returnValue.getReturnValue();
                if (NetworkHostFilter.unattached.equals(getViewFilterType())) {
                    final List<PairQueryable<VdsNetworkInterface, VDS>> items = new ArrayList<>();
                    for (Object obj : returnList) {
                        items.add(new PairQueryable<VdsNetworkInterface, VDS>(null, (VDS) obj));
                    }
                    setItems(items);
                } else if (NetworkHostFilter.attached.equals(getViewFilterType())) {
                    initAttachedInterfaces((Collection<PairQueryable<VdsNetworkInterface, VDS>>) returnList);
                }
            }
        });

        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());

        if (NetworkHostFilter.unattached.equals(getViewFilterType())) {
            Frontend.getInstance().runQuery(QueryType.GetVdsWithoutNetwork, params, asyncQuery);
        } else if (NetworkHostFilter.attached.equals(getViewFilterType())) {
            Frontend.getInstance().runQuery(QueryType.GetVdsAndNetworkInterfacesByNetworkId, params, asyncQuery);
        }

        setIsQueryFirstTime(false);
    }

    private void initAttachedInterfaces(final Collection<PairQueryable<VdsNetworkInterface, VDS>> items) {
        if (getEntity() == null || StringHelper.isNullOrEmpty(getEntity().getLabel()) || items == null || items.isEmpty()) {
            setItems(items);
            return;
        }

        final NetworkHostFilter filter = getViewFilterType();

        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(QueryType.GetInterfacesByLabelForNetwork,
            params,
            new AsyncQuery<QueryReturnValue>(returnValueObj -> {
                if (!filter.equals(getViewFilterType())) {
                    return;
                }

                attachedByLabelInterfaces = returnValueObj.getReturnValue();
                setItems(items);
            }));
    }

    public Boolean isInterfaceAttachedByLabel(VdsNetworkInterface iface) {
        return attachedByLabelInterfaces != null && attachedByLabelInterfaces.contains(iface);
    }

    public void setupNetworks() {
        if (getWindow() != null) {
            return;
        }

        HostSetupNetworksModel setupNetworksWindowModel =
                new HostSetupNetworksModel(this,
                        getSelectedItem().getSecond());
        setWindow(setupNetworksWindowModel);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getSetupNetworksCommand()) {
            setupNetworks();
        }

    }

    private void updateActionAvailability() {
        Collection<PairQueryable<VdsNetworkInterface, VDS>> selectedItems =
                getSelectedItems() != null ? getSelectedItems() : new ArrayList();

        getSetupNetworksCommand().setIsExecutionAllowed(selectedItems.size() == 1);
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
        search();
    }

    @Override
    protected String getListName() {
        return "NetworkHostListModel"; //$NON-NLS-1$
    }

}
