package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchStringCollector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.TagEventCollector;

public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(SearchStringCollector.class).asEagerSingleton();
        bind(TagEventCollector.class).asEagerSingleton();
    }
}
