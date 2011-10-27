package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplatePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstrctSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.inject.Inject;

public class SubTabTemplatePermissionView extends AbstrctSubTabPermissionsView<VmTemplate, TemplateListModel>
        implements SubTabTemplatePermissionPresenter.ViewDef {

    @Inject
    public SubTabTemplatePermissionView(SearchableDetailModelProvider<permissions, TemplateListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
