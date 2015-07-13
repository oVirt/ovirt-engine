package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class UserQuotaListModel extends SearchableListModel<DbUser, Quota> {
    public UserQuotaListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());
        setHelpTag(HelpTag.quota);
        setHashName("quota"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        super.syncSearch();
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                UserQuotaListModel quotaListModel = (UserQuotaListModel) model;
                quotaListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotasByAdElementIdQueryParameters parameters = new GetQuotasByAdElementIdQueryParameters();
        parameters.setAdElementId(getEntity().getId());

        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(VdcQueryType.GetQuotasByAdElementId,
                parameters,
                asyncQuery);
    }

    @Override
    protected String getListName() {
        return "UserQuotaListModel"; //$NON-NLS-1$
    }
}
