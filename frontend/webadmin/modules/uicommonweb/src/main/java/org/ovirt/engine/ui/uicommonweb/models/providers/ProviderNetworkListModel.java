package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ProviderNetworkListModel extends SearchableListModel {

    public ProviderNetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().providerNetworksTitle());
        setHashName("networks"); //$NON-NLS-1$
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

}
