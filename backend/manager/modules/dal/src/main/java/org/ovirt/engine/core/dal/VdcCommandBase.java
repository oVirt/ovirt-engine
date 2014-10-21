package org.ovirt.engine.core.dal;

import java.util.Arrays;
import java.util.Map;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged(errorLevel = LogLevel.WARN)
public abstract class VdcCommandBase {

    @Deprecated
    protected Log log = LogFactory.getLog(getClass());
    private final Logger log1 = LoggerFactory.getLogger(getClass());

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
        LoggedUtils.logEntry(log1, logId, this);

        try {
            executeCommand();
            LoggedUtils.logReturn(log1, logId, this, getReturnValue() != null && getReturnValue() instanceof Map[] ? Arrays.asList((Map[])getReturnValue()) : getReturnValue());
        } catch (Exception e) {
            LoggedUtils.logError(log1, logId, this, e);
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
}
