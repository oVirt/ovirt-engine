package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class PoolInterfaceListModelProvider extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, PoolInterfaceListModel> {

    @Inject
    public PoolInterfaceListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver,
            CurrentUser user) {
        super(ginjector, parentModelProvider, PoolInterfaceListModel.class, resolver, user);
    }

    @Override
    protected PoolInterfaceListModel createModel() {
        return new PoolInterfaceListModel();
    }

}
