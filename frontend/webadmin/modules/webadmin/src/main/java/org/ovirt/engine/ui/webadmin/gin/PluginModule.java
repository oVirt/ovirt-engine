package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.plugin.PluginEventHandler;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import org.ovirt.engine.ui.webadmin.plugin.ui.PluginUiFunctions;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabCustomPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabCustomProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabCustomView;

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

        // Bind MainTabCustomView to its view interface as non-singleton, without presenter binding
        // (MainTabCustomPresenter is created manually during runtime by MainTabCustomPresenterProvider)
        bind(MainTabCustomPresenter.ViewDef.class).to(MainTabCustomView.class);
        bind(MainTabCustomProxyFactory.class).in(Singleton.class);
    }

}
