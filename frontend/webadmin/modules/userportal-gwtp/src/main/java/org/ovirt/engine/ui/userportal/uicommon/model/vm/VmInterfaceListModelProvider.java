package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class VmInterfaceListModelProvider extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, VmInterfaceListModel> {

    @Inject
    public VmInterfaceListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, VmInterfaceListModel.class, resolver);
    }

    @Override
    protected VmInterfaceListModel createModel() {
        return new VmInterfaceListModel();
    }

}
