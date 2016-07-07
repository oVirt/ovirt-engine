package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmPoolSimpleUserCommandBase<T extends VmPoolSimpleUserParameters> extends VmPoolCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmPoolSimpleUserCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolSimpleUserCommandBase(T parameters, CommandContext commandContext) {
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
            dbUser = getDbUserDao().get(getAdUserId());
        }

        return dbUser;
    }

    protected void setDbUser(DbUser value) {
        dbUser = value;
    }

    private String adUserName;

    public String getAdUserName() {
        if (adUserName == null) {
            DbUser user = getDbUserDao().get(getAdUserId());
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
