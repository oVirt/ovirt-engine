package org.ovirt.engine.core.dal;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;

@Logged(errorLevel = LogLevel.WARN)
public abstract class VdcCommandBase {

    protected String getCommandName() {
        return getClass().getSimpleName().replace("Command", "");
    }

    public Object getReturnValue() {
        return null;
    }

    public void setReturnValue(Object value) {
    }

    public void execute() {
        String logId = LoggedUtils.getObjectId(this);
        LoggedUtils.logEntry(log, logId, this);

        try {
            executeCommand();
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

    protected abstract void executeCommand();

    public Object executeWithReturnValue() {
        execute();
        return getReturnValue();
    }

    protected Log log = LogFactory.getLog(getClass());
}
