package org.ovirt.engine.core.dal;

import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.utils.transaction.RollbackHandler;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Logged(errorLevel = LogLevel.WARN)
public abstract class VdcCommandBase implements RollbackHandler {

    private boolean getTransactive() {
        // Object[] attributes = new Object[] {}; //FIXED
        // getClass().GetCustomAttributes(TransactiveAttribute.class, true);
        TransactiveAttribute annotation = getClass().getAnnotation(TransactiveAttribute.class);
        return annotation != null;
    }

    protected String getCommandName() {
        return getClass().getSimpleName().replace("Command", "");
    }

    public Object getReturnValue() {
        return null;
    }

    public void setReturnValue(Object value) {
    }

    public void Execute() {
        String logId = LoggedUtils.getObjectId(this);
        LoggedUtils.logEntry(log, logId, this);

        if (getTransactive() && TransactionSupport.current() != null) {
            TransactionSupport.registerRollbackHandler(this);
        }

        try {
            ExecuteCommand();
            LoggedUtils.logReturn(log, logId, this, getReturnValue());
        } catch (Exception e) {
            LoggedUtils.logError(log, logId, this, e);
            // throw e;
            IllegalStateException ise = new IllegalStateException();
            ise.setStackTrace(e.getStackTrace());
            throw ise;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected abstract void ExecuteCommand();

    public Object ExecuteWithReturnValue() {
        Execute();
        return getReturnValue();
    }

    @Override
    public void Rollback() {
        log.errorFormat("Command {1} Rollbacked", getCommandName());
    }

    protected LogCompat log = LogFactoryCompat.getLog(getClass());
}
