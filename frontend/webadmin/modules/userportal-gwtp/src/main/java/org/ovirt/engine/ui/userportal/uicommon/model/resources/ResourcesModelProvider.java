package org.ovirt.engine.ui.userportal.uicommon.model.resources;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;

public class ResourcesModelProvider extends UserPortalDataBoundModelProvider<VM, ResourcesModel> {

    @Inject
    public ResourcesModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    protected ResourcesModel createModel() {
        return new ResourcesModel();
    }

}
