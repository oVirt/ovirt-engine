package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleManager;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITimer;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpPlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;

import com.google.inject.Inject;

public class UiCommonDefaultTypeResolver implements ITypeResolver {

    private final Configurator configurator;
    private final ILogger logger;

    private final ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister;
    private final ConsoleUtils consoleUtils;
    private final ConsoleManager consoleManager;
    private final ErrorPopupManager errorPopupManager;

    @Inject
    public UiCommonDefaultTypeResolver(Configurator configurator, ILogger logger,
            ConsoleUtils consoleUtils, ConsoleManager consoleManager, ErrorPopupManager errorPopupManager,
            ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister) {
        this.configurator = configurator;
        this.logger = logger;
        this.consoleOptionsFrontendPersister = consoleOptionsFrontendPersister;
        this.consoleUtils = consoleUtils;
        this.consoleManager = consoleManager;
        this.errorPopupManager = errorPopupManager;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object resolve(Class type) {
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
        } else if (type == IRdpPlugin.class) {
            return new RdpPluginImpl();
        } else if (type == IRdpNative.class) {
            return new RdpNativeImpl();
        } else if (type == ConsoleOptionsFrontendPersister.class) {
            return consoleOptionsFrontendPersister;
        } else if (type == ConsoleUtils.class) {
            return consoleUtils;
        } else if (type == ConsoleManager.class) {
            return consoleManager;
        } else if (type == ErrorPopupManager.class) {
            return errorPopupManager;
        }

        throw new RuntimeException("UiCommon Resolver cannot resolve type: " + type); //$NON-NLS-1$
    }

}
