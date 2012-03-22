package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.template.DisksTree;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabTemplateDiskView extends AbstractSubTabTreeView<DisksTree, VmTemplate, DiskModel, TemplateListModel, TemplateDiskListModel> implements SubTabTemplateDiskPresenter.ViewDef {

    @Inject
    public SubTabTemplateDiskView(final SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> modelProvider,
            EventBus eventBus) {
        super(modelProvider);

        actionPanel.addActionButton(new UiCommandButtonDefinition<DiskModel>(eventBus, "Copy") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getCopyCommand();
            }
        });

        setIsActionTree(true);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "Alias", "");
        table.addColumn(new EmptyColumn(), "Size", "120px");
        table.addColumn(new EmptyColumn(), "Status", "120px");
        table.addColumn(new EmptyColumn(), "Allocation", "120px");
        table.addColumn(new EmptyColumn(), "Interface", "120px");
        table.addColumn(new EmptyColumn(), "Creation Date", "120px");
    }

    @Override
    protected DisksTree getTree() {
        return new DisksTree(resources, constants);
    }

    @Override
    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return new SubTabTreeActionPanel<DiskModel>(modelProvider, ClientGinjectorProvider.instance().getEventBus());
    }
}
