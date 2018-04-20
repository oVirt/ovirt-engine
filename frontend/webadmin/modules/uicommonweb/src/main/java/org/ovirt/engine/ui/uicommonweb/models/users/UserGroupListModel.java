package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UserGroupListModel extends SearchableListModel<DbUser, UserGroup> {

    public UserGroupListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().directoryGroupsTitle());
        setHelpTag(HelpTag.directory_groups);
        setHashName("directory_groups"); // $//$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            final ArrayList<UserGroup> items = new ArrayList<>();
            AsyncDataProvider.getInstance().getAuthzGroupsByUserId(new AsyncQuery<>(returnValue -> {
                for (AuthzGroup grp : returnValue) {
                    items.add(createUserGroup(grp.getName(), grp.getNamespace(), grp.getAuthz()));
                }
                setItems(items);
            }), getEntity().getId());
        } else {
            setItems(null);
        }
    }

    private UserGroup createUserGroup(String groupFullName, String namespace, String authz) {
        UserGroup tempVar = new UserGroup();
        tempVar.setGroupName(groupFullName);
        tempVar.setNamespace(namespace);
        tempVar.setAuthz(authz);
        return tempVar;
    }

    @Override
    protected String getListName() {
        return "UserGroupListModel"; //$NON-NLS-1$
    }
}
