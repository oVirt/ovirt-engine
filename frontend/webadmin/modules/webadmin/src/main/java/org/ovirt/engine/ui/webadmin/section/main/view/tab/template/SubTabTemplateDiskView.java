package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.template.DisksTree;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class SubTabTemplateDiskView extends AbstractSubTabTreeView<DisksTree, VmTemplate, DiskModel, TemplateListModel, TemplateDiskListModel> implements SubTabTemplateDiskPresenter.ViewDef {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabTemplateDiskView(final SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> modelProvider,
            EventBus eventBus) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), constants.aliasDisk(), ""); //$NON-NLS-1$
        ImageResourceHeader readOnlyColumnHeader = new ImageResourceHeader(
                resources.readOnlyDiskIcon(), SafeHtmlUtils.fromSafeConstant(constants.readOnly()));
        table.addColumn(new EmptyColumn(), readOnlyColumnHeader, "60px"); //$NON-NLS-1$);
        table.addColumn(new EmptyColumn(), constants.provisionedSizeDisk(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.sizeDisk(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.statusDisk(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.allocationDisk(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.interfaceDisk(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.typeDisk(), "190px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.creationDateDisk(), "150px"); //$NON-NLS-1$
        table.setHeight("30px"); // $NON-NLS-1$
    }

    @Override
    protected DisksTree getTree() {
        return new DisksTree();
    }
}
