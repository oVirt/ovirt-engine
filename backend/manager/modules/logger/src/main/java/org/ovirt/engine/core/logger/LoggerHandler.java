package org.ovirt.engine.core.logger;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.logger.Logger;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class LoggerHandler extends Handler implements Observer {

    private volatile List<ExtensionProxy> extensions;

    public LoggerHandler() {
        EngineExtensionsManager.getInstance().addObserver(this);
        update(null, null);
    }

    @Override
    public void publish(LogRecord record) {
        ExtMap publishInputMap = new ExtMap()
                .mput(Base.InvokeKeys.COMMAND, Logger.InvokeCommands.PUBLISH)
                .mput(Logger.InvokeKeys.LOG_RECORD, record);
        ExtMap publishOutputMap = new ExtMap();
        for (ExtensionProxy extension : extensions) {
            try {
                extension.invoke(
                        publishInputMap,
                        publishOutputMap);
            } catch(Exception ex) {
                // ignore, logging this exception will result in infinite loop
            }
        }
    }

    @Override
    public void flush() {
        for (ExtensionProxy extension : extensions) {
            try {
                extension.invoke(new ExtMap()
                        .mput(Base.InvokeKeys.COMMAND, Logger.InvokeCommands.FLUSH),
                        true,
                        true);
            } catch(Exception ex) {
                // ignore, logging this exception will result in infinite loop
            }
        }
    }

    @Override
    public void close() throws SecurityException {
        for (ExtensionProxy extension : extensions) {
            try {
                extension.invoke(new ExtMap()
                        .mput(Base.InvokeKeys.COMMAND, Logger.InvokeCommands.CLOSE),
                        true,
                        true);
            } catch(Exception ex) {
                // ignore, logging this exception will result in infinite loop
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        extensions = EngineExtensionsManager.getInstance().getExtensionsByService(Logger.class.getName());
    }
}
