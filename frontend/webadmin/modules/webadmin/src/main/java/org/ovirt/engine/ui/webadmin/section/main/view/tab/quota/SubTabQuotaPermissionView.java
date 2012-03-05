package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaPermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaPermissionView extends AbstractSubTabTableView<Quota, permissions, QuotaListModel, QuotaPermissionListModel>
        implements SubTabQuotaPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaPermissionView(SearchableDetailModelProvider<permissions, QuotaListModel, QuotaPermissionListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    private void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumnWithTooltip<permissions> userColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        getTable().addColumn(userColumn, "User");

        TextColumnWithTooltip<permissions> roleColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        getTable().addColumn(roleColumn, "Role");

        TextColumnWithTooltip<permissions> permissionColumn = new ObjectNameColumn<permissions>() {
            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getDetailModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        getTable().addColumn(permissionColumn, "Inherited From");

        getTable().addActionButton(new WebAdminButtonDefinition<permissions>("Add") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<permissions>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
