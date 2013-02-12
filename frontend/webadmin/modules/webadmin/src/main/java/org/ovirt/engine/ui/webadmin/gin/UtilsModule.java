package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.utils.ConsoleManager;
import org.ovirt.engine.ui.common.utils.ConsoleUtils;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;


public class UtilsModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ConsoleUtils.class).in(Singleton.class);
        bind(ConsoleManager.class).in(Singleton.class);
    }

}
