package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class VmEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalListModel, VmEventListModel> {

    @Inject
    public VmEventListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, VmEventListModel.class, resolver);
    }

    @Override
    protected VmEventListModel createModel() {
        return new VmEventListModel();
    }

}
