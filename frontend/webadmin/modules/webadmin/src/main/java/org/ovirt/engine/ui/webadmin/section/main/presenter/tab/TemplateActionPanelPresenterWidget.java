package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateActionPanelPresenterWidget extends ActionPanelPresenterWidget<VmTemplate, TemplateListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TemplateActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<VmTemplate> view,
            MainModelProvider<VmTemplate, TemplateListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.editTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.removeTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        // Export operations drop down
        List<ActionButtonDefinition<VmTemplate>> exportSubActions = new LinkedList<>();
        exportSubActions.add(new WebAdminButtonDefinition<VmTemplate>(constants.exportToExportDomain()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportCommand();
            }
        });
        exportSubActions.add(new WebAdminButtonDefinition<VmTemplate>(constants.exportToOva()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportOvaCommand();
            }
        });
        // Add export menu bar
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.exportTemplate(),
                exportSubActions), exportSubActions);

        addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.createVmFromTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCreateVmFromTemplateCommand();
            }
        });
    }
}
