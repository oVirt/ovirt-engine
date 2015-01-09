package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaUserView extends AbstractSubTabTableView<Quota, Permissions, QuotaListModel, QuotaUserListModel>
        implements SubTabQuotaUserPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaUserView(SearchableDetailModelProvider<Permissions, QuotaListModel, QuotaUserListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Permissions> userColumn = new AbstractTextColumnWithTooltip<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        getTable().addColumn(userColumn, constants.userUser(), "400px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Permissions> permissionColumn = new AbstractObjectNameColumn<Permissions>() {
            @Override
            protected Object[] getRawValue(Permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getDetailModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.inheritedFromUser(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<Permissions>(constants.addUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Permissions>(constants.removeUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
