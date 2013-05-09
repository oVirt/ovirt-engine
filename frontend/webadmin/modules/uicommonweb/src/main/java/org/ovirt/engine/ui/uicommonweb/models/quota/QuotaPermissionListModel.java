package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaPermissionListModel extends PermissionListModel {

    public QuotaPermissionListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHashName("permissions"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(getObjectType());
        tempVar.setDirectOnly(false);
        tempVar.setRefresh(getIsQueryFirstTime());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                ArrayList<permissions> list =
                        (ArrayList<permissions>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                ArrayList<permissions> newList = new ArrayList<permissions>();
                for (permissions permission : list) {
                    if (!permission.getrole_id().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                        newList.add(permission);
                    }
                }
                searchableListModel.setItems(newList);
            }
        };

        tempVar.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetPermissionsForObject, tempVar, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected String getListName() {
        return "QuotaPermissionListModel"; //$NON-NLS-1$
    }

}
