package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
                mAdUserName = user.getLoginName();
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

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
