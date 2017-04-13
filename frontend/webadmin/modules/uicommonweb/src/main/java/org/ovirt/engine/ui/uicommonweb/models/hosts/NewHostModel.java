package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NewHostModel extends HostModel {

    public static final int NewHostDefaultPort = 54321;

    public NewHostModel() {
        getExternalHostName().getSelectedItemChangedEvent().addListener((ev, sender, args) -> hostName_SelectedItemChanged());

        getExternalHostName().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getExternalDiscoveredHosts().getSelectedItemChangedEvent().addListener((ev, sender, args) -> discoverHostName_SelectedItemChanged());

        getExternalDiscoveredHosts().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        getExternalHostProviderEnabled().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviders().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            // While load don't let user to change provider
            getProviders().setIsChangeable(false);
            providers_SelectedItemChanged();
            updateHostList();
        });

        getExternalHostGroups().getSelectedItemChangedEvent().addListener((ev, sender, args) -> externalHostGroups_SelectedItemChanged());

        getIsDiscoveredHosts().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (Boolean.TRUE.equals(getIsDiscoveredHosts().getEntity())) {
                discoverHostName_SelectedItemChanged();
            } else if (Boolean.FALSE.equals(getIsDiscoveredHosts().getEntity())) {
                hostName_SelectedItemChanged();
            }
        });

        getProviders().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getProviderSearchFilterLabel().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        getExternalHostProviderEnabled().setEntity(false);
        setEnableSearchHost(false);
    }

    // Define events:

    private void hostName_SelectedItemChanged() {
        if (Boolean.FALSE.equals(getIsDiscoveredHosts().getEntity())) {
            VDS vds = getExternalHostName().getSelectedItem();
            if (vds != null) {
                setOriginalName(vds.getName());
                getName().setEntity(vds.getName());
                getHost().setEntity(vds.getHostName());
            }
        }
    }

    private void discoverHostName_SelectedItemChanged() {
        if (Boolean.TRUE.equals(getIsDiscoveredHosts().getEntity())) {
            ExternalDiscoveredHost dhost = (ExternalDiscoveredHost) getExternalDiscoveredHosts().getSelectedItem();
            ExternalHostGroup dhg = (ExternalHostGroup) getExternalHostGroups().getSelectedItem();
            if (dhost != null && dhg != null) {
                setOriginalName(dhost.getName());
                getName().setEntity(dhost.getName());
                getHost().setEntity(dhost.getName() + "." + //$NON-NLS-1$
                        (dhg.getDomainName() != null ? dhg.getDomainName() : "")); //$NON-NLS-1$
            }
        }
    }

    private void externalHostGroups_SelectedItemChanged() {
        ExternalHostGroup dhg = (ExternalHostGroup) getExternalHostGroups().getSelectedItem();
        if (dhg != null) {
            getHost().setEntity(getName().getEntity() + "." + //$NON-NLS-1$
                    (dhg.getDomainName() != null ? dhg.getDomainName() : "")); //$NON-NLS-1$
        }
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

        AsyncDataProvider.getInstance().getExternalProviderHostList(new AsyncQuery<>(hosts -> {
            ListModel<VDS> hostNameListModel = getExternalHostName();
            hostNameListModel.setItems(hosts);
            hostNameListModel.setIsChangeable(true);
            setEnableSearchHost(true);
            getProviders().setIsChangeable(true);
        }), provider.getId(), true, getProviderSearchFilter().getEntity());

        AsyncDataProvider.getInstance().getExternalProviderHostGroupList(new AsyncQuery<>(hostGroups -> {
            ListModel externalHostGroupsListModel = getExternalHostGroups();
            externalHostGroupsListModel.setItems(hostGroups);
            externalHostGroupsListModel.setIsChangeable(true);

            AsyncDataProvider.getInstance().getExternalProviderDiscoveredHostList(new AsyncQuery<>(hosts -> {
                ListModel externalDiscoveredHostsListModel = getExternalDiscoveredHosts();
                externalDiscoveredHostsListModel.setItems(hosts);
                externalDiscoveredHostsListModel.setIsChangeable(true);
            }), getProviders().getSelectedItem());
        }), provider);

        AsyncDataProvider.getInstance().getExternalProviderComputeResourceList(new AsyncQuery<>(computeResources -> {
            ListModel externalComputeResourceListModel = getExternalComputeResource();
            externalComputeResourceListModel.setItems(computeResources);
            externalComputeResourceListModel.setIsChangeable(true);
        }), provider);
    }

    private void updateDiscoveredHostList(String searchFilter) {
        Provider provider = getProviders().getSelectedItem();
        if (provider != null ) {
            AsyncDataProvider.getInstance().getExternalProviderHostList(new AsyncQuery<>(hosts -> {
                ListModel<VDS> hostNameListModel = getExternalHostName();
                hostNameListModel.setItems(hosts);
                hostNameListModel.setIsChangeable(true);
                setEnableSearchHost(true);
            }), provider.getId(), true, searchFilter);
        } else {
            getExternalHostName().setItems(null);
            getExternalHostName().setIsChangeable(false);
            setEnableSearchHost(false);
        }
    }

    @Override
    protected boolean showInstallationProperties() {
        return true;
    }

    @Override
    protected void updateModelDataCenterFromVds(List<StoragePool> dataCenters, VDS vds) {
    }

    @Override
    protected void setAllowChangeHost(VDS vds) {
        if (getHost().getEntity() != null) {
            getHost().setIsChangeable(false);
        } else {
            getHost().setIsChangeable(true);
        }
    }

    @Override
    protected void setAllowChangeHostPlacementPropertiesWhenNotInMaintenance() {
        getDataCenter().setIsChangeable(true);
        getCluster().setIsChangeable(true);
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
    protected void updateModelClusterFromVds(ArrayList<Cluster> clusters, VDS vds) {
    }

    private void setEnableSearchHost(boolean value) {
        getProviderSearchFilter().setIsChangeable(value);
        getProviderSearchFilterLabel().setIsChangeable(value);
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

    protected void cpuVendorChanged() {
        resetKernelCmdline();
    }
}
