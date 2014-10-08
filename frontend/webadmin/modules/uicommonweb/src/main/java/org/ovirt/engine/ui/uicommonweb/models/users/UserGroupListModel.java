package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class UserGroupListModel extends SearchableListModel
{

    @Override
    public DbUser getEntity()
    {
        return (DbUser) ((super.getEntity() instanceof DbUser) ? super.getEntity() : null);
    }

    public void setEntity(DbUser value)
    {
        super.setEntity(value);
    }

    public UserGroupListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().directoryGroupsTitle());
        setHelpTag(HelpTag.directory_groups);
        setHashName("directory_groups"); // $//$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            final ArrayList<UserGroup> items = new ArrayList<UserGroup>();
            AsyncDataProvider.getDbGroupsByUserId(new AsyncQuery(new INewAsyncCallback() {

                @Override
                public void onSuccess(Object model, Object returnValue) {
                    for (DbGroup grp : (Collection<DbGroup>) returnValue) {
                        items.add(createUserGroup(grp.getName(), grp.getNamespace(), grp.getDomain()));
                    }
                    setItems(items);
                }
            }), getEntity().getId());
        }
        else
        {
            setItems(null);
        }
    }

    private UserGroup createUserGroup(String groupFullName, String namespace, String authz)
    {
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
