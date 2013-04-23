package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByImageIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskStorageListModel extends SearchableListModel
{
    public DiskStorageListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHashName("storage"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        DiskImage diskImage = (DiskImage) getEntity();
        if (diskImage == null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                searchableListModel.setItems((ArrayList<StorageDomain>) ((VdcQueryReturnValue) returnValue).getReturnValue());
            }
        };

        GetStorageDomainsByImageIdParameters getStorageDomainsByImageIdParameters =
                new GetStorageDomainsByImageIdParameters(diskImage.getImageId());
        getStorageDomainsByImageIdParameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByImageId, getStorageDomainsByImageIdParameters, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        DiskImage disk = (DiskImage) getEntity();
    }

    @Override
    protected String getListName() {
        return "DiskStorageListModel"; //$NON-NLS-1$

    }
}
