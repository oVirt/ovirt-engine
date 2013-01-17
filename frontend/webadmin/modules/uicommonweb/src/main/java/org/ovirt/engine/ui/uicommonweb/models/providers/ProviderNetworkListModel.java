package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ProviderNetworkListModel extends SearchableListModel {

    private static final String CMD_DISCOVER = "Discover"; //$NON-NLS-1$

    private UICommand privateDiscoverCommand;

    public ProviderNetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().providerNetworksTitle());
        setHashName("networks"); //$NON-NLS-1$
        setDiscoverCommand(new UICommand(CMD_DISCOVER, this));
    }

    public UICommand getDiscoverCommand() {
        return privateDiscoverCommand;
    }

    private void setDiscoverCommand(UICommand value) {
        privateDiscoverCommand = value;
    }

    @Override
    protected String getListName() {
        return "ProviderNetworkListModel"; //$NON-NLS-1$
    }

    @Override
    public Provider getEntity() {
        return (Provider) super.getEntity();
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
        DiscoverNetworksModel discoverModel = new DiscoverNetworksModel(this, getEntity());
        setWindow(discoverModel);
        discoverModel.initBackendData();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getDiscoverCommand()) {
            discover();
        }
    }

}
