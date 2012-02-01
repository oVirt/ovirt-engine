package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.view.client.ProvidesKey;

public class UserPortalItemModelKeyProvider implements ProvidesKey<UserPortalItemModel> {

    @Override
    public Object getKey(UserPortalItemModel item) {
        if (item == null) {
            return null;
        }
        return item.getIsPool() ? ((vm_pools) item.getEntity()).getvm_pool_id() : ((VM) item.getEntity()).getvm_guid();
    }

}
