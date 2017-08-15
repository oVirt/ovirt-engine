package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.plugin.PluginEventHandler;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import org.ovirt.engine.ui.webadmin.plugin.api.PluginUiFunctions;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DynamicUrlContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.DynamicUrlContentTabView;
import org.ovirt.engine.ui.webadmin.section.main.view.DynamicUrlContentView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.DynamicUrlContentPopupView;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * GIN module containing WebAdmin UI plugin infrastructure and related GWTP presenter bindings.
 */
public class PluginModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bind(PluginManager.class).asEagerSingleton();
        bind(PluginEventHandler.class).asEagerSingleton();
        bind(PluginUiFunctions.class).in(Singleton.class);

        // Dynamic tab component
        bind(DynamicUrlContentTabPresenter.ViewDef.class).to(DynamicUrlContentTabView.class);
        bind(DynamicUrlContentTabProxyFactory.class).in(Singleton.class);
        bind(DynamicUrlContentPresenter.ViewDef.class).to(DynamicUrlContentView.class);
        bind(DynamicUrlContentProxyFactory.class).in(Singleton.class);

        // Dynamic dialog component
        bindPresenterWidget(DynamicUrlContentPopupPresenterWidget.class,
                DynamicUrlContentPopupPresenterWidget.ViewDef.class,
                DynamicUrlContentPopupView.class);
    }

}
