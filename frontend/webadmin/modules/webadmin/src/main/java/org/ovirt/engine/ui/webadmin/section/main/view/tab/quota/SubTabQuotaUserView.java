package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaUserView extends AbstractSubTabTableView<Quota, Permission, QuotaListModel, QuotaUserListModel>
        implements SubTabQuotaUserPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabQuotaUserView(SearchableDetailModelProvider<Permission, QuotaListModel, QuotaUserListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable() {
        getTable().enableColumnResizing();

        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        getTable().addColumn(userColumn, constants.userUser(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> permissionColumn = new AbstractObjectNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getDetailModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.inheritedFromUser(), "400px"); //$NON-NLS-1$
    }
}
