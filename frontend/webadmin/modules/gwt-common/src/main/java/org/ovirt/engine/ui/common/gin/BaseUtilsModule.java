package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.presenter.PluginActionButtonHandler;
import org.ovirt.engine.ui.common.utils.ConsoleOptionsFrontendPersisterImpl;
import org.ovirt.engine.ui.common.utils.ConsoleUtilsImpl;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class BaseUtilsModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ConsoleOptionsFrontendPersister.class).to(ConsoleOptionsFrontendPersisterImpl.class).in(Singleton.class);
        bind(ConsoleUtils.class).to(ConsoleUtilsImpl.class).in(Singleton.class);
        bind(PluginActionButtonHandler.class).asEagerSingleton();
    }

}
