package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class VmPoolSimpleUserCommandBase<T extends VmPoolSimpleUserParameters> extends VmPoolCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VmPoolSimpleUserCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolSimpleUserCommandBase(T parameters) {
        this(parameters, null);
    }

    public VmPoolSimpleUserCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected Guid getAdUserId() {
        return getPoolUserSimpleParameters().getUserId();
    }

    @Override
    protected String getDescription() {
        return getAdUserName();
    }

    private DbUser mDbUser;

    protected DbUser getDbUser() {
        if (mDbUser == null) {
            mDbUser = DbFacade.getInstance().getDbUserDao().get(getAdUserId());
        }

        return mDbUser;
    }

    protected void setDbUser(DbUser value) {
        mDbUser = value;
    }

    private String mAdUserName;

    private VmPoolSimpleUserParameters getPoolUserSimpleParameters() {
        VdcActionParametersBase tempVar = getParameters();
        return (VmPoolSimpleUserParameters) ((tempVar instanceof VmPoolSimpleUserParameters) ? tempVar : null);
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

    public void setAdUserName(String value) {
        mAdUserName = value;
    }
}
