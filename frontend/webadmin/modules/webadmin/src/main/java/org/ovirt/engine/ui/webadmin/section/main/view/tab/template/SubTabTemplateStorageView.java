package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.template.StoragesTree;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabTemplateStorageView extends AbstractSubTabTreeView<StoragesTree, VmTemplate, StorageDomain, TemplateListModel, TemplateStorageListModel> implements SubTabTemplateStoragePresenter.ViewDef {

    @Inject
    public SubTabTemplateStorageView(final SearchableDetailModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> modelProvider,
            EventBus eventBus, ApplicationConstants constant, ApplicationTemplates templates, ApplicationResources resources) {
        super(modelProvider, constant, templates, resources);

        actionPanel.addActionButton(new UiCommandButtonDefinition<DiskModel>(eventBus, constant.removeStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });

        setIsActionTree(true);
    }

    @Override
    protected void initHeader(ApplicationConstants constants) {
        table.addColumn(new EmptyColumn(), constants.domainNameStorage(), ""); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.domainTypeStorage(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.statusStorage(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.freeSpaceStorage(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.usedSpaceStorage(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.totalSpaceStorage(), "120px"); //$NON-NLS-1$
    }

    @Override
    protected StoragesTree getTree() {
        return new StoragesTree(resources, constants, templates);
    }

    @Override
    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return new SubTabTreeActionPanel<DiskImage>(modelProvider, ClientGinjectorProvider.getEventBus());
    }
}
