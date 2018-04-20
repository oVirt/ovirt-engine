package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageLeaseListModel extends SearchableListModel<StorageDomain, VmStatic> {

    public StorageLeaseListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageDomainLeaseTitle());
        setHelpTag(HelpTag.leases);
        setHashName("leases"); //$NON-NLS-1$
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
        } else {
            setItems(null);
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        super.syncSearch();

        StorageDomain storageDomain = getEntity();
        Frontend.getInstance().runQuery(QueryType.GetEntitiesWithLeaseByStorageId,
                new IdQueryParameters(storageDomain.getId()), new SetItemsAsyncQuery());
    }

    @Override
    protected String getListName() {
        return "StorageLeaseListModel"; //$NON-NLS-1$
    }
}
