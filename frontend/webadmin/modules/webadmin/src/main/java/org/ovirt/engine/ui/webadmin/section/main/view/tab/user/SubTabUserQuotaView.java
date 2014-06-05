package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.comparators.QuotaComparator;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

public class SubTabUserQuotaView extends AbstractSubTabTableView<DbUser, Quota, UserListModel, UserQuotaListModel>
        implements SubTabUserQuotaPresenter.ViewDef {

    @Inject
    public SubTabUserQuotaView(SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new QuotaDcStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<Quota> nameColumn = new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable(QuotaComparator.NAME);
        getTable().addColumn(nameColumn, constants.nameQuota(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<Quota> descriptionColumn = new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descriptionColumn.makeSortable(QuotaComparator.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.descriptionQuota(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<Quota> datacenterColumn = new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName(); //$NON-NLS-1$
            }
        };
        datacenterColumn.makeSortable(QuotaComparator.DATA_CENTER);
        getTable().addColumn(datacenterColumn, constants.dcQuota(), "300px"); //$NON-NLS-1$
    }

}
