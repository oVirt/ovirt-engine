package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.LoggerImpl;
import org.ovirt.engine.ui.common.uicommon.NoVncImpl;
import org.ovirt.engine.ui.common.uicommon.RdpNativeImpl;
import org.ovirt.engine.ui.common.uicommon.RdpPluginImpl;
import org.ovirt.engine.ui.common.uicommon.SpiceNativeImpl;
import org.ovirt.engine.ui.common.uicommon.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.common.uicommon.VncNativeImpl;
import org.ovirt.engine.ui.common.uicommon.model.OptionsProvider;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.options.OptionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpPlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVncNative;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * GIN module containing common UiCommon integration bindings.
 */
public abstract class BaseUiCommonModule extends AbstractGinModule {

    protected void bindCommonIntegration() {
        bind(ITypeResolver.class).to(UiCommonDefaultTypeResolver.class).asEagerSingleton();
        bind(FrontendEventsHandlerImpl.class).in(Singleton.class);
        bind(FrontendFailureEventListener.class).in(Singleton.class);
        bind(ILogger.class).to(LoggerImpl.class).in(Singleton.class);

        // User Options
        bind(LocalStorage.class).to(ClientStorage.class);
        bind(OptionsModel.class).in(Singleton.class);

        bindCommonModelProviders();
        bindConsoles();
    }

    void bindCommonModelProviders() {
        // Options
        bind(OptionsProvider.class).in(Singleton.class);
    }
    protected void bindConfiguratorIntegration(Class<? extends Configurator> configurator) {
        bind(Configurator.class).to(configurator).in(Singleton.class);
    }

    private void bindConsoles() {
        bind(INoVnc.class).to(NoVncImpl.class);
        bind(IVncNative.class).to(VncNativeImpl.class);

        bind(ISpiceNative.class).to(SpiceNativeImpl.class);

        bind(IRdpNative.class).to(RdpNativeImpl.class);
        bind(IRdpPlugin.class).to(RdpPluginImpl.class);
    }
}
