package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.common.widget.tab.MenuLayout;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchStringCollector;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.google.inject.Singleton;

public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebadminMenuLayout.class).in(Singleton.class);
        bind(MenuLayout.class).in(Singleton.class);
        bind(SearchStringCollector.class).asEagerSingleton();
    }
}
