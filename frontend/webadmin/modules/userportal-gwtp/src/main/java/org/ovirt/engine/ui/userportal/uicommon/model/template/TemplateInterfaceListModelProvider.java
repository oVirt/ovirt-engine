package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class TemplateInterfaceListModelProvider
        extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalTemplateListModel, TemplateInterfaceListModel> {

    @Inject
    public TemplateInterfaceListModelProvider(ClientGinjector ginjector, UserPortalTemplateListProvider parentProvider, UserPortalModelResolver resolver) {
        super(ginjector, parentProvider, TemplateInterfaceListModel.class, resolver);
    }

    @Override
    protected TemplateInterfaceListModel createModel() {
        return new TemplateInterfaceListModel();
    }

}
