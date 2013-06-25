package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class NewHostModel extends HostModel {

    public static final int NewHostDefaultPort = 54321;
    public NewHostModel() {
        super();
        getExternalHostName().getSelectedItemChangedEvent().addListener(this);
        getExternalHostName().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getExternalHostProviderEnabled().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviders().getSelectedItemChangedEvent().addListener(this);
        getProviders().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilterLabel().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        IEventListener externalHostsListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                UpdateExternalHostModels();
            }
        };
        getExternalHostProviderEnabled().getEntityChangedEvent().addListener(externalHostsListener);
        getExternalHostProviderEnabled().setEntity(false);
        getExternalHostName().setIsChangable(false);
        setEnableSearchHost(false);
    }

    private void hostName_SelectedItemChanged()
    {
        VDS host = (VDS) getExternalHostName().getSelectedItem();

        if (host == null)
        {
            host = new VDS();
        }
        updateModelFromVds(host, null, false, null);
    }

    private void providers_SelectedItemChanged() {
        Provider provider = (Provider) getProviders().getSelectedItem();
        setEnableSearchHost(provider != null);
        getExternalHostName().setItems(null);
        getExternalHostName().setIsChangable(false);
    }

    private void updateHostList(String searchFilter) {
        Provider provider = (Provider) getProviders().getSelectedItem();
        if (provider != null ) {
            AsyncQuery getHostsQuery = new AsyncQuery();
            getHostsQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    ArrayList<VDS> hosts = (ArrayList<VDS>) result;
                    ListModel hostNameListModel = getExternalHostName();
                    hosts.add(0, null);
                    hostNameListModel.setItems(hosts);
                    hostNameListModel.setIsChangable(true);
                    setEnableSearchHost(true);
                }
            };
            AsyncDataProvider.GetExternalProviderHostList(getHostsQuery, provider.getId(), true, searchFilter);
        } else {
            getExternalHostName().setItems(null);
            getExternalHostName().setIsChangable(false);
            setEnableSearchHost(false);
        }
    }

    private void UpdateExternalHostModels()
    {
        boolean enabled = (Boolean) getExternalHostProviderEnabled().getEntity();
        if (enabled && getProviders().getItems() == null) {
            AsyncQuery getProvidersQuery = new AsyncQuery();
            getProvidersQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    ArrayList<Provider> providers = (ArrayList<Provider>) result;
                    ListModel providersListModel = getProviders();
                    providers.add(0, null);
                    providersListModel.setItems(providers);
                    providersListModel.setIsChangable(true);
                }
            };
            AsyncDataProvider.GetAllProvidersByType(getProvidersQuery, ProviderType.FOREMAN);
        } else {
            getProviders().setIsChangable(enabled);
            getProviders().setSelectedItem(null);
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getExternalHostName()) {
            hostName_SelectedItemChanged();
        } else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getProviders()) {
            providers_SelectedItemChanged();
        }
    }

    @Override
    protected boolean showInstallationProperties() {
        return true;
    }

    @Override
    protected void updateModelDataCenterFromVds(ArrayList<StoragePool> dataCenters, VDS vds) {
    }

    @Override
    protected void setAllowChangeHost(VDS vds) {
        if (getHost().getEntity() != null) {
            getHost().setIsChangable(false);
        } else {
            getHost().setIsChangable(true);
        }
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        getDataCenter().setIsChangable(true);
        getCluster().setIsChangable(true);
    }

    @Override
    protected void updateHosts() {
        updateHostList((String) getProviderSearchFilter().getEntity());
    }

    @Override
    public boolean showExternalProviderPanel() {
        return ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly;
    }

    @Override
    protected void setHostPort(VDS vds) {
        // If port is "0" then we set it to the default port
        if (vds.getPort() == 0) {
            getPort().setEntity(NewHostDefaultPort);
        }
    }

    @Override
    protected void updateModelClusterFromVds(ArrayList<VDSGroup> clusters, VDS vds) {
    }

    private void setEnableSearchHost(boolean value) {
        getProviderSearchFilter().setIsChangable(value);
        getProviderSearchFilterLabel().setIsChangable(value);
        getUpdateHostsCommand().setIsExecutionAllowed(value);
    }

    @Override
    public boolean showNetworkProviderTab() {
        return true;
    }
}
