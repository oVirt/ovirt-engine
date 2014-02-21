package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryManager;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;

public abstract class UserCommandBase<T extends IdParameters> extends CommandBase<T> {
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
        return getParameters().getId();
    }

    @SuppressWarnings("deprecation")
    public static DbUser initUser(String sessionId, String directoryName, ExternalId id) {
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().getByExternalId(directoryName, id);
        if (dbUser == null) {
            Directory directory = DirectoryManager.getInstance().getDirectory(directoryName);
            if (directory == null) {
                throw new VdcBLLException(VdcBllErrors.USER_FAILED_POPULATE_DATA);
            }
            DirectoryUser directoryUser = directory.findUser(id);
            if (directoryUser == null) {
                throw new VdcBLLException(VdcBllErrors.USER_FAILED_POPULATE_DATA);
            }
            dbUser = new DbUser(directoryUser);
            DbFacade.getInstance().getDbUserDao().save(dbUser);
        }
        return dbUser;
    }

    /**
     * Check if the authenticated user exist in the DB. Add it if its missing.
     */
    public static DbUser persistAuthenticatedUser(DirectoryUser directoryUser) {
        DbUserDAO dao = DbFacade.getInstance().getDbUserDao();
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
