package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.TemplatesTree;

import com.google.inject.Inject;

public class SubTabStorageTemplateView extends AbstractSubTabTreeView<TemplatesTree, storage_domains, VmTemplate, StorageListModel, StorageTemplateListModel>
        implements SubTabStorageTemplatePresenter.ViewDef {

    @Inject
    public SubTabStorageTemplateView(SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "Name");
        table.addColumn(new EmptyColumn(), "Disks", "110px");
        table.addColumn(new EmptyColumn(), "Actual Size", "110px");
        table.addColumn(new EmptyColumn(), "Creation Date", "170px");
    }

    @Override
    protected TemplatesTree getTree() {
        return new TemplatesTree(resources, constants);
    }

}
