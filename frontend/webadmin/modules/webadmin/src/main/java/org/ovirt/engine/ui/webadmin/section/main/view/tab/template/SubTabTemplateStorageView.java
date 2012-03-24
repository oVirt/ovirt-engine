package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.template.StoragesTree;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabTemplateStorageView extends AbstractSubTabTreeView<StoragesTree, VmTemplate, storage_domains, TemplateListModel, TemplateStorageListModel> implements SubTabTemplateStoragePresenter.ViewDef {

    @Inject
    public SubTabTemplateStorageView(final SearchableDetailModelProvider<storage_domains, TemplateListModel, TemplateStorageListModel> modelProvider,
            EventBus eventBus) {
        super(modelProvider);

        actionPanel.addActionButton(new UiCommandButtonDefinition<DiskModel>(eventBus, "Remove") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });

        setIsActionTree(true);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "Domain Name", "");
        table.addColumn(new EmptyColumn(), "Domain Type", "120px");
        table.addColumn(new EmptyColumn(), "Status", "120px");
        table.addColumn(new EmptyColumn(), "Free Space", "120px");
        table.addColumn(new EmptyColumn(), "Used Space", "120px");
        table.addColumn(new EmptyColumn(), "Total Space", "120px");
    }

    @Override
    protected StoragesTree getTree() {
        return new StoragesTree(resources, constants);
    }

    @Override
    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return new SubTabTreeActionPanel<DiskImage>(modelProvider, ClientGinjectorProvider.instance().getEventBus());
    }
}
