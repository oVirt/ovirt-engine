package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;

public abstract class VmPoolUserCommandBase<T extends VmPoolUserParameters> extends VmPoolCommandBase<T> {

    @Inject
    private DbUserDao dbUserDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmPoolUserCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolUserCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected Guid getAdUserId() {
        return getParameters().getUserId();
    }

    @Override
    protected String getDescription() {
        return getVmPoolName();
    }

    private DbUser dbUser;

    protected DbUser getDbUser() {
        if (dbUser == null) {
            dbUser = dbUserDao.get(getAdUserId());
        }

        return dbUser;
    }

    protected void setDbUser(DbUser value) {
        dbUser = value;
    }

    private String adUserName;

    public String getAdUserName() {
        if (adUserName == null) {
            DbUser user = dbUserDao.get(getAdUserId());
            if (user != null) {
                adUserName = user.getLoginName();
            }
        }

        return adUserName;
    }

    public void setAdUserName(String value) {
        adUserName = value;
    }

}
