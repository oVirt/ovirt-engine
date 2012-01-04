package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplatePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabTemplatePermissionView extends AbstractSubTabPermissionsView<VmTemplate, TemplateListModel>
        implements SubTabTemplatePermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabTemplatePermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabTemplatePermissionView(SearchableDetailModelProvider<permissions, TemplateListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

    @Override
    protected void initTable() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
        super.initTable();
    }

}
