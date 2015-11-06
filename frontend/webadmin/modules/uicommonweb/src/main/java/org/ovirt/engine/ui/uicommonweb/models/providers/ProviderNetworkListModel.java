package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class ProviderNetworkListModel extends SearchableListModel<Provider, NetworkView> {

    private static final String CMD_DISCOVER = "Discover"; //$NON-NLS-1$

    private UICommand discoverCommand;

    private final com.google.inject.Provider<CommonModel> commonModelProvider;

    @Inject
    public ProviderNetworkListModel(final com.google.inject.Provider<CommonModel> commonModelProvider) {
        this.commonModelProvider = commonModelProvider;
        setTitle(ConstantsManager.getInstance().getConstants().providerNetworksTitle());
        setHelpTag(HelpTag.networks);
        setHashName("networks"); //$NON-NLS-1$
        setDiscoverCommand(new UICommand(CMD_DISCOVER, this));
        setComparator(new NameableComparator());
    }

    public UICommand getDiscoverCommand() {
        return discoverCommand;
    }

    private void setDiscoverCommand(UICommand value) {
        discoverCommand = value;
    }

    @Override
    protected String getListName() {
        return "ProviderNetworkListModel"; //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        Provider provider = getEntity();
        if (provider == null) {
            return;
        }

        super.syncSearch(VdcQueryType.GetAllNetworksForProvider, new IdQueryParameters(provider.getId()));
    }

    private void discover() {
        if (getWindow() != null) {
            return;
        }
        DiscoverNetworksModel discoverModel = new DiscoverNetworksModel(this, getEntity(), commonModelProvider);
        setWindow(discoverModel);
        discoverModel.discoverNetworks();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getDiscoverCommand()) {
            discover();
        }
    }

}
