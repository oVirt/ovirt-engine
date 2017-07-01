package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class QuotaPermissionListModel extends PermissionListModel<Quota> {

    @Inject
    public QuotaPermissionListModel(Provider<AdElementListModel> adElementListModelProvider) {
        super(adElementListModelProvider);
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHelpTag(HelpTag.permissions);
        setHashName("permissions"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(getObjectType());
        tempVar.setDirectOnly(false);
        tempVar.setRefresh(getIsQueryFirstTime());

        tempVar.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetPermissionsForObject, tempVar, new AsyncQuery<QueryReturnValue>(returnValue -> {
            ArrayList<Permission> list = returnValue.getReturnValue();
            ArrayList<Permission> newList = new ArrayList<>();
            for (Permission permission : list) {
                if (!permission.getRoleId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                    newList.add(permission);
                }
            }
            setItems(newList);
        }));

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected String getListName() {
        return "QuotaPermissionListModel"; //$NON-NLS-1$
    }

}
