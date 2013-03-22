package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITimer;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdp;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;

import com.google.inject.Inject;

public class UiCommonDefaultTypeResolver implements ITypeResolver {

    private final Configurator configurator;
    private final ILogger logger;
    private final ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister;

    @Inject
    public UiCommonDefaultTypeResolver(Configurator configurator, ILogger logger, ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister) {
        this.configurator = configurator;
        this.logger = logger;
        this.consoleOptionsFrontendPersister = consoleOptionsFrontendPersister;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object Resolve(Class type) {
        if (type == Configurator.class) {
            return configurator;
        } else if (type == ILogger.class) {
            return logger;
        } else if (type == ITimer.class) {
            return new TimerImpl();
        } else if (type == ISpicePlugin.class) {
            return new SpicePluginImpl();
        } else if (type == ISpiceNative.class) {
            return new SpiceNativeImpl();
        } else if (type == IRdp.class) {
            return new IRdpImpl();
        } else if (type == ConsoleOptionsFrontendPersister.class) {
            return consoleOptionsFrontendPersister;
        }

        throw new RuntimeException("UiCommon Resolver cannot resolve type: " + type); //$NON-NLS-1$
    }

}
