package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.TemplatesTree;

public class SubTabQuotaTemplateView extends AbstractSubTabTreeView<TemplatesTree<QuotaTemplateListModel>, Quota, VmTemplate, QuotaListModel, QuotaTemplateListModel>
        implements SubTabQuotaTemplatePresenter.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabQuotaTemplateView(SearchableDetailModelProvider<VmTemplate, QuotaListModel, QuotaTemplateListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), constants.aliasTemplate());
        table.addColumn(new EmptyColumn(), constants.disksTemplate(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.actualSizeTemplate(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.creationDateTemplate(), "170px"); //$NON-NLS-1$
        table.setHeight("27px"); // $NON-NLS-1$
    }

    @Override
    protected TemplatesTree<QuotaTemplateListModel> getTree() {
        return new TemplatesTree<>();
    }

}
