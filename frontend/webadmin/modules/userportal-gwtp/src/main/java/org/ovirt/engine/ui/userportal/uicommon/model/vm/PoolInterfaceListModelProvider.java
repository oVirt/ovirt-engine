package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
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
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, PoolInterfaceListModel.class, resolver);
    }

    @Override
    protected PoolInterfaceListModel createModel() {
        return new PoolInterfaceListModel();
    }

}
