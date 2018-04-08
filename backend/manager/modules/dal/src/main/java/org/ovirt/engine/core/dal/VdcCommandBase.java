package org.ovirt.engine.core.dal;

import org.ovirt.engine.core.utils.ObjectDescriptor;
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
            StringBuilder builder = new StringBuilder();
            ObjectDescriptor.toStringBuilder(getReturnValue(), builder);
            LoggedUtils.logReturn(log, logId, this, builder.toString());
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
