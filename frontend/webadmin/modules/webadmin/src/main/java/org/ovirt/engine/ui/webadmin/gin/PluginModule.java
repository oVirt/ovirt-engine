package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.plugin.PluginEventHandler;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions;
import org.ovirt.engine.ui.webadmin.plugin.restapi.RestApiSessionManager;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.view.DynamicUrlContentTabView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * GIN module containing WebAdmin UI plugin infrastructure bindings.
 */
public class PluginModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(PluginManager.class).asEagerSingleton();
        bind(PluginEventHandler.class).asEagerSingleton();
        bind(PluginUiFunctions.class).in(Singleton.class);
        bind(RestApiSessionManager.class).in(Singleton.class);

        // Dynamic tab component bindings
        bind(DynamicUrlContentTabPresenter.ViewDef.class).to(DynamicUrlContentTabView.class);
        bind(DynamicUrlContentTabProxyFactory.class).in(Singleton.class);
    }

}
