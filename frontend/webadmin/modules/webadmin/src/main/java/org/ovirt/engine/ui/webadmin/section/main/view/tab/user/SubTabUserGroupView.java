package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.comparators.UserGroupComparator;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public class SubTabUserGroupView extends AbstractSubTabTableView<DbUser, UserGroup, UserListModel, UserGroupListModel>
        implements SubTabUserGroupPresenter.ViewDef {

    @Inject
    public SubTabUserGroupView(SearchableDetailModelProvider<UserGroup, UserListModel, UserGroupListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();
        TextColumnWithTooltip<UserGroup> nameColumn = new TextColumnWithTooltip<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getGroupName();
            }
        };
        nameColumn.makeSortable(UserGroupComparator.NAME);
        getTable().addColumn(nameColumn, constants.groupNameGroup(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<UserGroup> namespaceColumn = new TextColumnWithTooltip<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getNamespace();
            }
        };
        namespaceColumn.makeSortable(UserGroupComparator.NAMESPACE);
        getTable().addColumn(namespaceColumn, constants.namespaceGroup(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<UserGroup> authzColumn = new TextColumnWithTooltip<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getAuthz();
            }
        };
        authzColumn.makeSortable(UserGroupComparator.AUTHZ);
        getTable().addColumn(authzColumn, constants.authzGroup(), "300px"); //$NON-NLS-1$
    }

}
