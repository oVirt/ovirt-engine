package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByImageGuidParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class DiskTemplateListModel extends SearchableListModel
{
    public DiskTemplateListModel()
    {
        setTitle("Templates");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

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
            public void OnSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                HashMap map = (HashMap) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                searchableListModel.setItems((Iterable) map.get(true));
            }
        };

        GetVmTemplatesByImageGuidParameters getVmTemplatesByImageGuidParameters =
                new GetVmTemplatesByImageGuidParameters(diskImage.getImageId());
        getVmTemplatesByImageGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetVmTemplatesByImageGuid, getVmTemplatesByImageGuidParameters, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        DiskImage disk = (DiskImage) getEntity();
    }

    @Override
    protected String getListName() {
        return "DiskTemplateListModel";
    }
}
