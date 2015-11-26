package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITimer;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpPlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceHtml5;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVncNative;
import org.ovirt.engine.ui.uicommonweb.restapi.HasForeignMenuData;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UiCommonDefaultTypeResolver implements ITypeResolver {

    private final Configurator configurator;
    private final ILogger logger;

    private final ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister;
    private final ConsoleUtils consoleUtils;
    private final ErrorPopupManager errorPopupManager;
    private final CurrentUser currentUser;
    private final CurrentUserRole currentUserRole;

    // we inject providers for the console impls since they
    // contain state unique for each connect operation and thus
    // new instance is required each time
    private final Provider<ISpicePlugin> spicePluginProvider;
    private final Provider<ISpiceNative> spiceNativeProvider;
    private final Provider<ISpiceHtml5> spiceHtml5Provider;
    private final Provider<IRdpPlugin> rdpPluginProvider;
    private final Provider<IRdpNative> rdpNativeProvider;
    private final Provider<IVncNative> vncNativeProvider;
    private final Provider<INoVnc> noVncProvider;
    private final DynamicMessages dynamicMessages;

    @Inject
    public UiCommonDefaultTypeResolver(Configurator configurator, ILogger logger,
            ConsoleUtils consoleUtils,  ErrorPopupManager errorPopupManager,
            ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister,
            CurrentUser currentUser, CurrentUserRole currentUserRole,
            Provider<ISpicePlugin> spicePluginProvider, // deprecated in 4.0
            Provider<ISpiceNative> spiceNativeProvider,
            Provider<ISpiceHtml5> spiceHtml5Provider,
            Provider<IRdpPlugin> rdpPluginProvider,
            Provider<IRdpNative> rdpNativeProvider,
            Provider<IVncNative> vncNativeProvider,
            Provider<INoVnc> noVncProvider,
            DynamicMessages dynamicMessages) {
        this.configurator = configurator;
        this.logger = logger;
        this.consoleOptionsFrontendPersister = consoleOptionsFrontendPersister;
        this.consoleUtils = consoleUtils;
        this.errorPopupManager = errorPopupManager;
        this.currentUser = currentUser;
        this.currentUserRole = currentUserRole;

        this.spicePluginProvider = spicePluginProvider;
        this.spiceNativeProvider = spiceNativeProvider;
        this.spiceHtml5Provider = spiceHtml5Provider;
        this.rdpPluginProvider = rdpPluginProvider;
        this.rdpNativeProvider = rdpNativeProvider;
        this.vncNativeProvider = vncNativeProvider;
        this.noVncProvider = noVncProvider;
        this.dynamicMessages = dynamicMessages;
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
        } else if (type == ISpicePlugin.class) {// deprecated in 4.0
            return spicePluginProvider.get();
        } else if (type == ISpiceNative.class) {
            return withSsoToken(spiceNativeProvider.get());
        } else if (type == ISpiceHtml5.class) {
            return spiceHtml5Provider.get();
        } else if (type == IRdpPlugin.class) {
            return rdpPluginProvider.get();
        } else if (type == IRdpNative.class) {
            return rdpNativeProvider.get();
        } else if (type == INoVnc.class) {
            return noVncProvider.get();
        } else if (type == IVncNative.class) {
            return withSsoToken(vncNativeProvider.get());
        } else if (type == ConsoleOptionsFrontendPersister.class) {
            return consoleOptionsFrontendPersister;
        } else if (type == ConsoleUtils.class) {
            return consoleUtils;
        } else if (type == ErrorPopupManager.class) {
            return errorPopupManager;
        } else if (type == CurrentUserRole.class) {
            return currentUserRole;
        } else if (type == DynamicMessages.class) {
            return dynamicMessages;
        }

        throw new RuntimeException("UiCommon Resolver cannot resolve type: " + type); //$NON-NLS-1$
    }

    public <T extends HasForeignMenuData> T withSsoToken(T consoleImpl) {
        consoleImpl.setSsoToken(currentUser.getSsoToken());
        return consoleImpl;
    }

}
