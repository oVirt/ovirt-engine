package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class PoolDiskListModelProvider extends UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, PoolDiskListModel> {

    @Inject
    public PoolDiskListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver,
            CurrentUser user) {
        super(ginjector, parentModelProvider, PoolDiskListModel.class, resolver, user);
    }

    @Override
    protected PoolDiskListModel createModel() {
        return new PoolDiskListModel();
    }

}
