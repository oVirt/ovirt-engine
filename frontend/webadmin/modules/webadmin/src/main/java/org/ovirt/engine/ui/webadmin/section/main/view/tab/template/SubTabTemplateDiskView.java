package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.template.DisksTree;

import com.google.inject.Inject;

public class SubTabTemplateDiskView extends AbstractSubTabTreeView<DisksTree, VmTemplate, DiskModel, TemplateListModel, TemplateDiskListModel> implements SubTabTemplateDiskPresenter.ViewDef {

    @Inject
    public SubTabTemplateDiskView(SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "Name", "");
        table.addColumn(new EmptyColumn(), "Size", "120px");
        table.addColumn(new EmptyColumn(), "Type", "120px");
        table.addColumn(new EmptyColumn(), "Allocation", "120px");
        table.addColumn(new EmptyColumn(), "Interface", "120px");
    }

    @Override
    protected DisksTree getTree() {
        return new DisksTree();
    }
}
