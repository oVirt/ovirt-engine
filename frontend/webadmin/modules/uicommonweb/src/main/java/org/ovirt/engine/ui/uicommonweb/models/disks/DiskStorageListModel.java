package org.ovirt.engine.ui.uicommonweb.models.disks;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskStorageListModel extends SearchableListModel<DiskImage, StorageDomain> {
    public DiskStorageListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHelpTag(HelpTag.storage);
        setHashName("storage"); //$NON-NLS-1$
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
        DiskImage diskImage = getEntity();
        if (diskImage == null) {
            return;
        }

        IdQueryParameters getStorageDomainsByImageIdParameters = new IdQueryParameters(diskImage.getImageId());
        getStorageDomainsByImageIdParameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsByImageId, getStorageDomainsByImageIdParameters, new SetItemsAsyncQuery());

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
    }

    @Override
    protected String getListName() {
        return "DiskStorageListModel"; //$NON-NLS-1$

    }
}
