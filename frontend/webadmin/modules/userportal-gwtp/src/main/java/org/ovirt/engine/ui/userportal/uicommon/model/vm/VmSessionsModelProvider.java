package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.inject.Inject;

public class VmSessionsModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel> {

    @Inject
    public VmSessionsModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver modelResolver) {
        super(ginjector, parentModelProvider, VmSessionsModel.class, modelResolver);
    }

}
