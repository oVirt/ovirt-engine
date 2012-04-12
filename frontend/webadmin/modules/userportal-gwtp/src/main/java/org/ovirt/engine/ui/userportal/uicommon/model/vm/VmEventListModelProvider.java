package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class VmEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalListModel, UserPortalVmEventListModel> {

    @Inject
    public VmEventListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver,
            CurrentUser user) {
        super(ginjector, parentModelProvider, UserPortalVmEventListModel.class, resolver, user);
    }

    @Override
    protected UserPortalVmEventListModel createModel() {
        return new UserPortalVmEventListModel();
    }

}
