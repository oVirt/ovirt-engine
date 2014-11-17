package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
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
        getExternalHostName().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                hostName_SelectedItemChanged();
            }
        });

        getExternalHostName().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getExternalDiscoveredHosts().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                discoverHostName_SelectedItemChanged();
            }
        });

        getExternalDiscoveredHosts().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        getExternalHostProviderEnabled().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviders().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // While load don't let user to change provider
                getProviders().setIsChangable(false);
                providers_SelectedItemChanged();
                updateHostList();
            }
        });

        getProviders().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilterLabel().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getExternalHostProviderEnabled().setEntity(false);
        setEnableSearchHost(false);
    }

    // Define events:

    private void hostName_SelectedItemChanged()
    {
        VDS vds = (VDS) getExternalHostName().getSelectedItem();
        if (vds == null) {
            vds = new VDS();
        }
        updateModelFromVds(vds, null, false, null);
    }

    private void discoverHostName_SelectedItemChanged() {
        ExternalDiscoveredHost dhost = (ExternalDiscoveredHost) getExternalDiscoveredHosts().getSelectedItem();
        VDS vds = new VDS();
        if (dhost != null) {
            vds.setVdsName(dhost.getName());
            vds.setHostName(dhost.getIp());
        }
        updateModelFromVds(vds, null, false, null);
    }

    private void providers_SelectedItemChanged() {
        cleanHostParametersFields();
        Provider provider = getProviders().getSelectedItem();
        setEnableSearchHost(provider != null);
    }

    private void updateHostList() {
        Provider provider = getProviders().getSelectedItem();
        if (provider == null) {
            return;
        }
        AsyncQuery getHostsQuery = new AsyncQuery();
        getHostsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ArrayList<VDS> hosts = (ArrayList<VDS>) result;
                ListModel<VDS> hostNameListModel = getExternalHostName();
                hostNameListModel.setItems(hosts);
                hostNameListModel.setIsChangable(true);
                setEnableSearchHost(true);
                getProviders().setIsChangable(true);
            }
        };
        AsyncDataProvider.getExternalProviderHostList(getHostsQuery,
                provider.getId(),
                true,
                getProviderSearchFilter().getEntity());

        AsyncQuery getDiscoveredHostsQuery = new AsyncQuery();
        getDiscoveredHostsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ArrayList<ExternalDiscoveredHost> hosts = (ArrayList<ExternalDiscoveredHost>) result;
                ListModel externalDiscoveredHostsListModel = getExternalDiscoveredHosts();
                externalDiscoveredHostsListModel.setItems(hosts);
                externalDiscoveredHostsListModel.setIsChangable(true);
            }
        };
        AsyncDataProvider.getExternalProviderDiscoveredHostList(getDiscoveredHostsQuery, provider);

        AsyncQuery getHostGroupsQuery = new AsyncQuery();
        getHostGroupsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ArrayList<ExternalHostGroup> hostGroups = (ArrayList<ExternalHostGroup>) result;
                ListModel externalHostGroupsListModel = getExternalHostGroups();
                externalHostGroupsListModel.setItems(hostGroups);
                externalHostGroupsListModel.setIsChangable(true);
            }
        };
        AsyncDataProvider.getExternalProviderHostGroupList(getHostGroupsQuery, provider);

        AsyncQuery getComputeResourceQuery = new AsyncQuery();
        getComputeResourceQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                ArrayList<ExternalComputeResource> computeResources = (ArrayList<ExternalComputeResource>) result;
                ListModel externalComputeResourceListModel = getExternalComputeResource();
                externalComputeResourceListModel.setItems(computeResources);
                externalComputeResourceListModel.setIsChangable(true);
            }
        };
        AsyncDataProvider.getExternalProviderComputeResourceList(getComputeResourceQuery, provider);
    }

    private void updateDiscoveredHostList(String searchFilter) {
        Provider provider = getProviders().getSelectedItem();
        if (provider != null ) {
            AsyncQuery getHostsQuery = new AsyncQuery();
            getHostsQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    ArrayList<VDS> hosts = (ArrayList<VDS>) result;
                    ListModel<VDS> hostNameListModel = getExternalHostName();
                    hostNameListModel.setItems(hosts);
                    hostNameListModel.setIsChangable(true);
                    setEnableSearchHost(true);
                }
            };
            AsyncDataProvider.getExternalProviderHostList(getHostsQuery, provider.getId(), true, searchFilter);
        } else {
            getExternalHostName().setItems(null);
            getExternalHostName().setIsChangable(false);
            setEnableSearchHost(false);
        }
    }

    private void updateExternalHostModels() {
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                ArrayList<Provider> providers = (ArrayList<Provider>) result;
                ListModel<Provider> providersListModel = getProviders();
                providersListModel.setItems(providers, Linq.firstOrDefault(providers));
                providersListModel.setIsChangable(true);
                getIsDiscoveredHosts().setEntity(null);
                getIsDiscoveredHosts().setEntity(true);
            }
        };
        AsyncDataProvider.getAllProvidersByType(getProvidersQuery, ProviderType.FOREMAN);
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
    public void updateHosts() {
        updateExternalHostModels();
    }

    @Override
    protected void updateProvisionedHosts() {
        updateDiscoveredHostList(getProviderSearchFilter().getEntity());
    }

    @Override
    public boolean showExternalProviderPanel() {
        return ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly;
    }

    @Override
    protected void setPort(VDS vds) {
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
        return ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly;
    }

    @Override
    protected boolean editTransportProperties(VDS vds) {
        return true;
    }
}
