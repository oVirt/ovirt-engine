package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalTemplateListProvider;

import com.google.inject.Inject;

public class TemplateGeneralModelProvider extends UserPortalDetailModelProvider<UserPortalTemplateListModel, TemplateGeneralModel> {

    @Inject
    public TemplateGeneralModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentModelProvider) {
        super(ginjector, parentModelProvider, TemplateGeneralModel.class);
    }

}
