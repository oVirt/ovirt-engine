package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class ClusterPolicyClusterListModel extends SearchableListModel {
    public ClusterPolicyClusterListModel() {
        setSearchPageSize(1000);
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        AsyncQuery asyncQuery = new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                ClusterPolicyClusterListModel clusterPolicyClusterListModel = (ClusterPolicyClusterListModel) model;
                clusterPolicyClusterListModel.setItems((Iterable) ((VdcQueryReturnValue) returnValue).getReturnValue());
            }
        });

        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAttachedClustersByClusterPolicyId, params, asyncQuery);
        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        search();
    }

    @Override
    public ClusterPolicy getEntity() {
        return (ClusterPolicy) super.getEntity();
    }

    @Override
    protected String getListName() {
        return "ClusterPolicyClusterListModel"; //$NON-NLS-1$
    }

}
