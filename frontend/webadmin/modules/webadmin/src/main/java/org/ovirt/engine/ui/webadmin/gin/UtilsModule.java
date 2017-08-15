package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchStringCollector;

public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(SearchStringCollector.class).asEagerSingleton();
    }
}
