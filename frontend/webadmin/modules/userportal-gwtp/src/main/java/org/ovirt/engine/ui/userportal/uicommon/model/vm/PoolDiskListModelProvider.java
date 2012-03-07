package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class PoolDiskListModelProvider extends UserPortalSearchableDetailModelProvider<DiskImage, UserPortalListModel, PoolDiskListModel> {

    @Inject
    public PoolDiskListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, PoolDiskListModel.class, resolver);
    }

    @Override
    protected PoolDiskListModel createModel() {
        return new PoolDiskListModel();
    }

}
