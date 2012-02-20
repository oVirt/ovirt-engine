package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.inject.Inject;

public class TemplateGeneralModelProvider extends UserPortalDetailModelProvider<UserPortalTemplateListModel, TemplateGeneralModel> {

    @Inject
    public TemplateGeneralModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, TemplateGeneralModel.class, resolver);
    }

}
