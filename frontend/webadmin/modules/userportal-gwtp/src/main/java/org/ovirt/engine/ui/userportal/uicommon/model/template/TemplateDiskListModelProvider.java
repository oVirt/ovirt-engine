package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class TemplateDiskListModelProvider
        extends UserPortalSearchableDetailModelProvider<DiskImage, UserPortalTemplateListModel, TemplateDiskListModel> {

    @Inject
    public TemplateDiskListModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentProvider, TemplateDiskListModel.class, resolver);
    }

    @Override
    protected TemplateDiskListModel createModel() {
        return new TemplateDiskListModel();
    }

}
