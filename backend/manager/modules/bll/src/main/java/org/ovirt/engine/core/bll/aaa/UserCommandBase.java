package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;

public abstract class UserCommandBase<T extends IdParameters> extends CommandBase<T> {

    @Inject
    private DbUserDao dbUserDao;

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
            DbUser user = dbUserDao.get(getAdUserId());
            if (user != null) {
                adUserName = user.getLoginName();
            }
        }
        return adUserName;
    }

    protected Guid getAdUserId() {
        return getParameters().getId();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
