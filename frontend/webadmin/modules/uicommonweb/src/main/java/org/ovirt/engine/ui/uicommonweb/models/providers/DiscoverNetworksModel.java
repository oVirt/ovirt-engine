package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.ImportNetworksModel;

public class DiscoverNetworksModel extends ImportNetworksModel {

    private final Provider<?> provider;

    public DiscoverNetworksModel(SearchableListModel sourceListModel, Provider<?> provider) {
        super(sourceListModel);
        this.provider = provider;
        setHelpTag(HelpTag.discover_networks);
        setHashName("discover_networks"); //$NON-NLS-1$
        getProviders().setIsChangable(false);
    }

    public void discoverNetworks() {
        Collection<Provider<?>> items = new ArrayList<Provider<?>>();
        items.add(provider);
        getProviders().setItems(items);
        getProviders().setSelectedItem(provider);
    }

    @Override
    protected void initProviderList() {
        // do nothing, already have the only provider to be displayed in the list
    }

}
