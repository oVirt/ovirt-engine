package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.common.widget.tab.MenuLayout;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchStringCollector;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebadminMenuLayout.class).asEagerSingleton();
        bind(MenuLayout.class).asEagerSingleton();
        bind(SearchStringCollector.class).asEagerSingleton();
    }
}
