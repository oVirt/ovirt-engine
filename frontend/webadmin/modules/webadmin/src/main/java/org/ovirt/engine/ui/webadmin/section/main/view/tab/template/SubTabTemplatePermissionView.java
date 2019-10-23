package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplatePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabTemplatePermissionView extends AbstractSubTabPermissionsView<VmTemplate, TemplateListModel>
        implements SubTabTemplatePermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabTemplatePermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabTemplatePermissionView(SearchableDetailModelProvider<Permission, TemplateListModel,
            PermissionListModel<VmTemplate>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<VmTemplate, TemplateListModel, PermissionListModel<VmTemplate>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
