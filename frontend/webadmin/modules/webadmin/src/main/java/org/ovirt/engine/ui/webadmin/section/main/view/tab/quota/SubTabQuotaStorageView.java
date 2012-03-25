package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaStorageView extends AbstractSubTabTableView<Quota, QuotaStorage, QuotaListModel, QuotaStorageListModel>
        implements SubTabQuotaStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaStorageView(SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    private void initTable() {
        getTable().addColumn(new TextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                return object.getStorageName();
            }
        }, "Name");

        getTable().addColumn(new TextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                return (object.getStorageSizeGBUsage() == null ? "0" : object.getStorageSizeGBUsage().toString()) + "/"
                        + (object.getStorageSizeGB() == null ? "*" : object.getStorageSizeGB().toString()) + " GB";
            }
        },
                "Used Storage/Total");
    }
}
