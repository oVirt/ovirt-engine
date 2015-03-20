package org.ovirt.engine.ui.common.uicommon;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.ovirt.engine.ui.common.restapi.RestApiSessionAcquiredEvent;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
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

public class UiCommonDefaultTypeResolver implements ITypeResolver, RestApiSessionAcquiredEvent.RestApiSessionAcquiredHandler {

    private final Configurator configurator;
    private final ILogger logger;

    private final ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister;
    private final ConsoleUtils consoleUtils;
    private final ErrorPopupManager errorPopupManager;
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

    private String sessionId;

    @Inject
    public UiCommonDefaultTypeResolver(Configurator configurator, ILogger logger,
            ConsoleUtils consoleUtils,  ErrorPopupManager errorPopupManager,
            ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister,
            CurrentUserRole currentUserRole,
            EventBus eventBus,
            Provider<ISpicePlugin> spicePluginProvider,
            Provider<ISpiceNative> spiceNativeProvider,
            Provider<ISpiceHtml5> spiceHtml5Provider,
            Provider<IRdpPlugin> rdpPluginProvider,
            Provider<IRdpNative> rdpNativeProvider,
            Provider<IVncNative> vncNativeProvider,
            Provider<INoVnc> noVncProvider) {
        this.configurator = configurator;
        this.logger = logger;
        this.consoleOptionsFrontendPersister = consoleOptionsFrontendPersister;
        this.consoleUtils = consoleUtils;
        this.errorPopupManager = errorPopupManager;
        this.currentUserRole = currentUserRole;

        this.spicePluginProvider = spicePluginProvider;
        this.spiceNativeProvider = spiceNativeProvider;
        this.spiceHtml5Provider = spiceHtml5Provider;
        this.rdpPluginProvider = rdpPluginProvider;
        this.rdpNativeProvider = rdpNativeProvider;
        this.vncNativeProvider = vncNativeProvider;
        this.noVncProvider = noVncProvider;

        eventBus.addHandler(RestApiSessionAcquiredEvent.getType(), this);
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
            return spicePluginProvider.get();
        } else if (type == ISpiceNative.class) {
            return withSessionId(spiceNativeProvider.get());
        } else if (type == ISpiceHtml5.class) {
            return spiceHtml5Provider.get();
        } else if (type == IRdpPlugin.class) {
            return rdpPluginProvider.get();
        } else if (type == IRdpNative.class) {
            return rdpNativeProvider.get();
        } else if (type == INoVnc.class) {
            return noVncProvider.get();
        } else if (type == IVncNative.class) {
            return withSessionId(vncNativeProvider.get());
        } else if (type == ConsoleOptionsFrontendPersister.class) {
            return consoleOptionsFrontendPersister;
        } else if (type == ConsoleUtils.class) {
            return consoleUtils;
        } else if (type == ErrorPopupManager.class) {
            return errorPopupManager;
        } else if (type == CurrentUserRole.class) {
            return currentUserRole;
        }

        throw new RuntimeException("UiCommon Resolver cannot resolve type: " + type); //$NON-NLS-1$
    }

    public <T extends HasForeignMenuData> T withSessionId(T consoleImpl) {
        consoleImpl.setSessionId(sessionId);
        return consoleImpl;
    }

    @Override
    public void onRestApiSessionAcquired(RestApiSessionAcquiredEvent event) {
        this.sessionId = event.getSessionId();
    }
}
