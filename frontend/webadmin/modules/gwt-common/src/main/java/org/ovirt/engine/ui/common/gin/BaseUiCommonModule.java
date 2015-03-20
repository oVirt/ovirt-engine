package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.LoggerImpl;
import org.ovirt.engine.ui.common.uicommon.NoVncImpl;
import org.ovirt.engine.ui.common.uicommon.RdpNativeImpl;
import org.ovirt.engine.ui.common.uicommon.RdpPluginImpl;
import org.ovirt.engine.ui.common.uicommon.SpiceHtml5Impl;
import org.ovirt.engine.ui.common.uicommon.SpiceNativeImpl;
import org.ovirt.engine.ui.common.uicommon.SpicePluginImpl;
import org.ovirt.engine.ui.common.uicommon.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.common.uicommon.VncNativeImpl;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpPlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceHtml5;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVncNative;

/**
 * GIN module containing common UiCommon integration bindings.
 */
public abstract class BaseUiCommonModule extends AbstractGinModule {

    protected void bindCommonIntegration() {
        bind(ITypeResolver.class).to(UiCommonDefaultTypeResolver.class).asEagerSingleton();
        bind(FrontendEventsHandlerImpl.class).in(Singleton.class);
        bind(FrontendFailureEventListener.class).in(Singleton.class);
        bind(ILogger.class).to(LoggerImpl.class).in(Singleton.class);

        bindConsoles();
    }

    protected void bindConfiguratorIntegration(Class<? extends Configurator> configurator) {
        bind(Configurator.class).to(configurator).in(Singleton.class);
    }

    private void bindConsoles() {
        bind(INoVnc.class).to(NoVncImpl.class);
        bind(IVncNative.class).to(VncNativeImpl.class);

        bind(ISpiceHtml5.class).to(SpiceHtml5Impl.class);
        bind(ISpiceNative.class).to(SpiceNativeImpl.class);
        bind(ISpicePlugin.class).to(SpicePluginImpl.class);

        bind(IRdpNative.class).to(RdpNativeImpl.class);
        bind(IRdpPlugin.class).to(RdpPluginImpl.class);
    }
}
