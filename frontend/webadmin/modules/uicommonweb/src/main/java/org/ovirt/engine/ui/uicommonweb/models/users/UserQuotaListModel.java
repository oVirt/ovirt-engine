package org.ovirt.engine.ui.uicommonweb.models.users;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
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
        GetQuotasByAdElementIdQueryParameters parameters = new GetQuotasByAdElementIdQueryParameters();
        parameters.setAdElementId(getEntity().getId());

        parameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetQuotasByAdElementId,
                parameters,
                new SetItemsAsyncQuery());
    }

    @Override
    protected String getListName() {
        return "UserQuotaListModel"; //$NON-NLS-1$
    }
}
