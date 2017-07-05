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
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaPermissionView extends AbstractSubTabTableView<Quota, Permission, QuotaListModel, QuotaPermissionListModel>
        implements SubTabQuotaPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabQuotaPermissionView(SearchableDetailModelProvider<Permission, QuotaListModel, QuotaPermissionListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        getTable().addColumn(userColumn, constants.userPermission());

        AbstractTextColumn<Permission> roleColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable();
        getTable().addColumn(roleColumn, constants.rolePermission());

        AbstractTextColumn<Permission> permissionColumn = new AbstractObjectNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getDetailModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.inheretedFromPermission());
    }

}
