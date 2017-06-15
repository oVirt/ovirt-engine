package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.comparators.QuotaComparator;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabUserQuotaView extends AbstractSubTabTableView<DbUser, Quota, UserListModel, UserQuotaListModel>
        implements SubTabUserQuotaPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserQuotaView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabUserQuotaView(SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
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
        nameColumn.makeSortable(QuotaComparator.NAME);
        getTable().addColumn(nameColumn, constants.nameQuota(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> descriptionColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descriptionColumn.makeSortable(QuotaComparator.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.descriptionQuota(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> datacenterColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName(); //$NON-NLS-1$
            }
        };
        datacenterColumn.makeSortable(QuotaComparator.DATA_CENTER);
        getTable().addColumn(datacenterColumn, constants.dcQuota(), "300px"); //$NON-NLS-1$
    }

}
