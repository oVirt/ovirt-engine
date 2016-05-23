package org.ovirt.engine.core.dal;

import java.util.Arrays;
import java.util.Map;

import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged(errorLevel = LogLevel.WARN)
public abstract class VdcCommandBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

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
            LoggedUtils.logReturn(log, logId, this, getReturnValue() != null && getReturnValue() instanceof Map[] ? Arrays.asList((Map[])getReturnValue()) : getReturnValue());
        } catch (Exception e) {
            LoggedUtils.logError(log, logId, this, e);
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
}
