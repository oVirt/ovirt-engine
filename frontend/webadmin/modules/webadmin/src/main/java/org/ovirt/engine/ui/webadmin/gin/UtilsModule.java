package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.common.utils.ConsoleManager;
import org.ovirt.engine.ui.common.utils.ConsoleUtils;

import com.google.inject.Singleton;


public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(ConsoleUtils.class).in(Singleton.class);
        bind(ConsoleManager.class).in(Singleton.class);
    }

}
