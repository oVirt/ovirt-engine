package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("AdUserName") })
public abstract class UserCommandBase<T extends AdElementParametersBase> extends CommandBase<T> {
    public UserCommandBase() {
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected UserCommandBase(Guid commandId) {
        super(commandId);
    }

    public UserCommandBase(T parameters) {
        super(parameters);

    }

    private String mAdUserName;

    @Override
    protected String getDescription() {
        return getAdUserName();
    }

    public String getAdUserName() {
        if (mAdUserName == null) {
            DbUser user = DbFacade.getInstance().getDbUserDao().get(getAdUserId());
            if (user != null) {
                mAdUserName = user.getusername();
            }
        }
        return mAdUserName;
    }

    protected Guid getAdUserId() {
        return getParameters().getAdElementId();
    }

    @SuppressWarnings("deprecation")
    public static DbUser initUser(VdcUser vdcUser, String sessionId) {
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().get(vdcUser.getUserId());
        if (dbUser == null) {
            LdapUser adUser = (LdapUser) LdapFactory
                    .getInstance(vdcUser.getDomainControler())
                    .RunAdAction(AdActionType.GetAdUserByUserId,
                            new LdapSearchByIdParameters(sessionId, vdcUser.getDomainControler(), vdcUser.getUserId()))
                    .getReturnValue();
            if (adUser == null) {
                throw new VdcBLLException(VdcBllErrors.USER_FAILED_POPULATE_DATA);
            }
            dbUser = new DbUser(adUser);
            DbFacade.getInstance().getDbUserDao().save(dbUser);
        }
        return dbUser;
    }

    /**
     * Check if the authenticated user exist in the DB. Add it if its missing.
     *
     * @param ldapUser
     * @return newly create
     */
    public static DbUser persistAuthenticatedUser(LdapUser ldapUser) {
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().get(ldapUser.getUserId());
        boolean newUser = dbUser == null;
        dbUser = new DbUser(ldapUser);
        if (newUser) {
            DbFacade.getInstance().getDbUserDao().save(dbUser);
        } else {
            DbFacade.getInstance().getDbUserDao().update(dbUser);
        }
        return dbUser;
    }

    /**
     * Process on changing VdcRole to user/group. First remove on current permissions since them not relevant anymore.
     * Second if new user/group role are User or PowerUser - add to it default permission with specific role.
     */
    public static void ProcessAdElementDefaultRole(Guid adElementId, String userName) {
        SearchParameters p = new SearchParameters(String.format("user:name = %1$s or usrname = %2$s", userName,
                userName), SearchType.DBUser);
        p.setMaxCount(Integer.MAX_VALUE);
        List<IVdcQueryable> elements = (List<IVdcQueryable>) Backend.getInstance()
                .runInternalQuery(VdcQueryType.Search, p).getReturnValue();
        DbUser adElement;
        if (elements != null && elements.size() > 0
                && (adElement = (DbUser) ((elements.get(0) instanceof DbUser) ? elements.get(0) : null)) != null) {
            for (permissions permission : DbFacade.getInstance().getPermissionDao().getAllForAdElement(adElementId)) {
                Backend.getInstance().runInternalAction(VdcActionType.RemovePermission,
                        new PermissionsOperationsParametes(permission));
            }
        }
    }

    public static boolean CanAttachVmTo(Guid vmId, java.util.ArrayList<String> message) {
        boolean returnValue = true;
        VmStatic vmStatic = DbFacade.getInstance().getVmStaticDao().get(vmId);
        if (vmStatic == null) {
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.toString());
            returnValue = false;
        }

        if (DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(vmId) != null) {
            returnValue = false;
            message.add(VdcBllMessages.USER_CANNOT_ATTACH_TO_VM_IN_POOL.toString());
        }

        return returnValue;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
