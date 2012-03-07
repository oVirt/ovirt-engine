package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.inject.Inject;

public class PoolGeneralModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, PoolGeneralModel> {

    @Inject
    public PoolGeneralModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, PoolGeneralModel.class, resolver);
    }

}
