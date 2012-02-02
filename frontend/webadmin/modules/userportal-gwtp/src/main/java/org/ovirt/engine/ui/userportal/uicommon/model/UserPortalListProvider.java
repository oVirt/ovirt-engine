package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;

import com.google.inject.Inject;

public class UserPortalListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalListModel> {

    @Inject
    public UserPortalListProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    public Object getKey(UserPortalItemModel item) {
        return item.getIsPool() ? ((vm_pools) item.getEntity()).getvm_pool_id() : ((VM) item.getEntity()).getvm_guid();
    }

    @Override
    protected UserPortalListModel createModel() {
        return new UserPortalListModel();
    }

}
