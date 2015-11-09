package org.ovirt.engine.core.bll.aaa;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

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

    @SuppressWarnings("deprecation")
    public static DbUser initUser(String sessionId, String directoryName, String id) {
        DbUser dbUser = DbFacade.getInstance().getDbUserDao().getByExternalId(directoryName, id);
        if (dbUser == null) {
            ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(directoryName);
            if (authz == null) {
                throw new EngineException(EngineError.USER_FAILED_POPULATE_DATA);
            }
            boolean foundUser = false;
            for (String namespace : authz.getContext().<Collection<String>>get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
                DirectoryUser directoryUser = DirectoryUtils.findDirectoryUserById(authz, namespace, id, false, false);
                if (directoryUser != null) {
                    dbUser = new DbUser(directoryUser);
                    dbUser.setId(Guid.newGuid());
                    DbFacade.getInstance().getDbUserDao().save(dbUser);
                    foundUser = true;
                    break;
                }
            }
            if (!foundUser) {
                throw new EngineException(EngineError.USER_FAILED_POPULATE_DATA);
            }
        }
        return dbUser;
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
