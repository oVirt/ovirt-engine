package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetQuotaClusterByQuotaId,
                params,
                asyncQuery);
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
