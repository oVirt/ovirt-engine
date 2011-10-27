package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabUserGroupView extends AbstractSubTabTableView<DbUser, UserGroup, UserListModel, UserGroupListModel>
        implements SubTabUserGroupPresenter.ViewDef {

    @Inject
    public SubTabUserGroupView(SearchableDetailModelProvider<UserGroup, UserListModel, UserGroupListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<UserGroup> nameColumn = new TextColumn<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getGroupName();
            }
        };
        getTable().addColumn(nameColumn, "Group Name");

        TextColumn<UserGroup> OrgUnitColumn = new TextColumn<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getOrganizationalUnit();
            }
        };
        getTable().addColumn(OrgUnitColumn, "Organizational Unit");

        TextColumn<UserGroup> domainColumn = new TextColumn<UserGroup>() {
            @Override
            public String getValue(UserGroup object) {
                return object.getDomain();
            }
        };
        getTable().addColumn(domainColumn, "Domain");

    }

}
