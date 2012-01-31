package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.LoggerImpl;
import org.ovirt.engine.ui.common.uicommon.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * GIN module containing common UiCommon integration bindings.
 */
public abstract class BaseUiCommonModule extends AbstractGinModule {

    protected void bindCommonIntegration() {
        bind(ITypeResolver.class).to(UiCommonDefaultTypeResolver.class).in(Singleton.class);
        bind(FrontendEventsHandlerImpl.class).in(Singleton.class);
        bind(FrontendFailureEventListener.class).in(Singleton.class);
        bind(ILogger.class).to(LoggerImpl.class).in(Singleton.class);
    }

    protected void bindConfiguratorIntegration(Class<? extends Configurator> configurator) {
        bind(Configurator.class).to(configurator).in(Singleton.class);
    }

}
