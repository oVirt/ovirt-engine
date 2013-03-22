package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

public class SubTabDataCenterQuotaView extends AbstractSubTabTableView<storage_pool, Quota, DataCenterListModel, DataCenterQuotaListModel>
        implements SubTabDataCenterQuotaPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterQuotaView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterQuotaView(SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new QuotaDcStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        }, constants.nameQuota());

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        }, constants.descriptionQuota());

        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.addQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCreateCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.editQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.copyQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCloneCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.removeQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
