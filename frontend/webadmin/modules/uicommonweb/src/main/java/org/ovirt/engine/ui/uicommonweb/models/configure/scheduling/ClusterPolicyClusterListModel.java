package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class ClusterPolicyClusterListModel extends SearchableListModel<ClusterPolicy, Cluster> {
    public ClusterPolicyClusterListModel() {
        setSearchPageSize(1000);
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetAttachedClustersByClusterPolicyId, params, new SetItemsAsyncQuery());
        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        search();
    }

    @Override
    protected String getListName() {
        return "ClusterPolicyClusterListModel"; //$NON-NLS-1$
    }

}
