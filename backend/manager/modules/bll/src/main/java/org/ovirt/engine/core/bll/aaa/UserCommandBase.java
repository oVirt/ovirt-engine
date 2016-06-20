package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDao;

public abstract class UserCommandBase<T extends IdParameters> extends CommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected UserCommandBase(Guid commandId) {
        super(commandId);
    }

    public UserCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private String adUserName;

    @Override
    protected String getDescription() {
        return getAdUserName();
    }

    public String getAdUserName() {
        if (adUserName == null) {
            DbUser user = DbFacade.getInstance().getDbUserDao().get(getAdUserId());
            if (user != null) {
                adUserName = user.getLoginName();
            }
        }
        return adUserName;
    }

    protected Guid getAdUserId() {
        return getParameters().getId();
    }

    /**
     * Check if the authenticated user exist in the DB. Add it if its missing.
     */
    public static DbUser persistAuthenticatedUser(DirectoryUser directoryUser) {
        DbUserDao dao = DbFacade.getInstance().getDbUserDao();
        DbUser dbUser = dao.getByExternalId(directoryUser.getDirectoryName(), directoryUser.getId());
        if (dbUser != null) {
            dao.update(dbUser);
        }
        else {
            dbUser = new DbUser(directoryUser);
            dbUser.setId(Guid.newGuid());
            dao.save(dbUser);
        }
        return dbUser;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
