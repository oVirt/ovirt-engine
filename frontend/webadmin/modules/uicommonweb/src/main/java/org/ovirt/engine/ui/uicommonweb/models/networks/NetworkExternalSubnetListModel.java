package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class NetworkExternalSubnetListModel extends SearchableListModel
{

    public NetworkExternalSubnetListModel() {
        setHashName("external_subnets"); //$NON-NLS-1$
        setComparator(new NameableComparator());
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) super.getEntity();
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
        if (getEntity() == null) {
            return;
        }

        super.syncSearch(VdcQueryType.GetExternalSubnetsOnProviderByNetwork, new IdQueryParameters(getEntity().getId()));
    }

    @Override
    protected String getListName() {
        return "NetworkExternalSubnetListModel"; //$NON-NLS-1$
    }

}
