package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;

import com.google.inject.Inject;

public class VmGeneralModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, VmGeneralModel> {

    @Inject
    public VmGeneralModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider) {
        super(ginjector, parentModelProvider, VmGeneralModel.class);
    }

}
