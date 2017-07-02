package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaClusterListModel extends SearchableListModel<Quota, Quota> {

    public QuotaClusterListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().clustersTitle());
        setHelpTag(HelpTag.clusters);
        setHashName("clusters"); // $//$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        Frontend.getInstance().runQuery(QueryType.GetQuotaClusterByQuotaId,
                params,
                new SetItemsAsyncQuery());
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        if (getEntity() == null) {
            return;
        }
        getSearchCommand().execute();
    }

    @Override
    protected String getListName() {
        return "QuotaClusterListModel"; //$NON-NLS-1$
    }

}
