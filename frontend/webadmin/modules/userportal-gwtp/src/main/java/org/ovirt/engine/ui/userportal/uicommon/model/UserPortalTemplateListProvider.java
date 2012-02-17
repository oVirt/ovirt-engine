package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;

import com.google.inject.Inject;

public class UserPortalTemplateListProvider extends UserPortalDataBoundModelProvider<VmTemplate, UserPortalTemplateListModel> {

    @Inject
    public UserPortalTemplateListProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    // TODO get key is not correct on the parent
    @Override
    protected UserPortalTemplateListModel createModel() {
        return new UserPortalTemplateListModel();
    }

}
