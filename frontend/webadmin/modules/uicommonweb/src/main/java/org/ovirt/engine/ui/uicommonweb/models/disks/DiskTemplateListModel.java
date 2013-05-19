package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskTemplateListModel extends SearchableListModel
{
    public DiskTemplateListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHashName("templates"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch()
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
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                HashMap<Boolean, VmTemplate> map = (HashMap<Boolean, VmTemplate>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                List<VmTemplate> templates = new ArrayList<VmTemplate>();
                templates.add(map.get(true));
                searchableListModel.setItems(templates);
            }
        };

        IdQueryParameters getVmTemplatesByImageGuidParameters = new IdQueryParameters(diskImage.getImageId());
        getVmTemplatesByImageGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetVmTemplatesByImageGuid, getVmTemplatesByImageGuidParameters, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability()
    {
    }

    @Override
    protected String getListName() {
        return "DiskTemplateListModel"; //$NON-NLS-1$
    }
}
