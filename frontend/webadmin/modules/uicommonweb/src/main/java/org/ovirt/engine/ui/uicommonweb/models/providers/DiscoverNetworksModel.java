package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.BaseImportNetworksModel;

import com.google.inject.Inject;

public class DiscoverNetworksModel extends BaseImportNetworksModel {

    private final Provider<?> provider;

    @Inject
    public DiscoverNetworksModel(ProviderNetworkListModel sourceListModel, Provider<?> provider,
            DataCenterListModel dataCenterListModel) {
        super(sourceListModel, dataCenterListModel);
        this.provider = provider;
        setHelpTag(HelpTag.discover_networks);
        setHashName("discover_networks"); //$NON-NLS-1$
        getProviders().setIsChangeable(false);
    }

    public void discoverNetworks() {
        Collection<Provider<?>> items = new ArrayList<>();
        items.add(provider);
        getProviders().setItems(items);
        getProviders().setSelectedItem(provider);
    }

    @Override
    protected void initProviderList() {
        // do nothing, already have the only provider to be displayed in the list
    }

}
