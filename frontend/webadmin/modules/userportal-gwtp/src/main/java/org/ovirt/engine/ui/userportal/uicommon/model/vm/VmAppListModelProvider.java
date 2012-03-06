package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class VmAppListModelProvider extends UserPortalSearchableDetailModelProvider<String, UserPortalListModel, VmAppListModel> {

    @Inject
    public VmAppListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, VmAppListModel.class, resolver);
    }

    @Override
    protected VmAppListModel createModel() {
        return new VmAppListModel();
    }

}
