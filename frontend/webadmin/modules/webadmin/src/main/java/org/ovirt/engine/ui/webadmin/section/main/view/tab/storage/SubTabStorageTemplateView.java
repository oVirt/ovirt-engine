package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.TemplatesTree;

import com.google.inject.Inject;

public class SubTabStorageTemplateView extends AbstractSubTabTreeView<TemplatesTree<StorageTemplateListModel>, StorageDomain, VmTemplate, StorageListModel, StorageTemplateListModel>
        implements SubTabStorageTemplatePresenter.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageTemplateView(SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), constants.aliasTemplate());
        table.addColumn(new EmptyColumn(), constants.disksTemplate(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.provisionedSizeTemplate(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.actualSizeTemplate(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.creationDateTemplate(), "170px"); //$NON-NLS-1$
        table.setHeight("55px"); // $NON-NLS-1$
    }

    @Override
    protected TemplatesTree<StorageTemplateListModel> getTree() {
        return new TemplatesTree<>();
    }

}
