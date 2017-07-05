package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabDataCenterQuotaView extends AbstractSubTabTableView<StoragePool, Quota, DataCenterListModel, DataCenterQuotaListModel>
        implements SubTabDataCenterQuotaPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterQuotaView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterQuotaView(SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        QuotaDcStatusColumn dcStatusColumn = new QuotaDcStatusColumn();
        dcStatusColumn.setContextMenuTitle(constants.dcStatusQuota());
        getTable().addColumn(dcStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> nameColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameQuota(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> descriptionColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionQuota(), "400px"); //$NON-NLS-1$
    }

}
