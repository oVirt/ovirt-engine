package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUtilsModule;
import org.ovirt.engine.ui.common.utils.ConsoleManagerImpl;
import org.ovirt.engine.ui.common.utils.ConsoleUtilsImpl;
import org.ovirt.engine.ui.uicommonweb.ConsoleManager;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;

import com.google.inject.Singleton;


public class UtilsModule extends BaseUtilsModule {

    @Override
    protected void configure() {
        super.configure();
        bind(ConsoleUtils.class).to(ConsoleUtilsImpl.class).in(Singleton.class);
        bind(ConsoleManager.class).to(ConsoleManagerImpl.class).in(Singleton.class);
    }

}
