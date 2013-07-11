package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.ImportNetworksModel;

public class DiscoverNetworksModel extends ImportNetworksModel {

    private final Provider provider;

    public DiscoverNetworksModel(SearchableListModel sourceListModel, Provider provider) {
        super(sourceListModel);
        this.provider = provider;
        setHashName("discover_networks"); //$NON-NLS-1$
        getProviders().setIsChangable(false);
    }

    public void discoverNetworks() {
        getProviders().setItems(Collections.singletonList(provider));
        getProviders().setSelectedItem(provider);
    }

    @Override
    protected void initProviderList() {
        // do nothing, already have the only provider to be displayed in the list
    }

}
