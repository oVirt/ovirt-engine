package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.utils.ConsoleOptionsFrontendPersisterImpl;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class BaseUtilsModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ConsoleOptionsFrontendPersister.class).to(ConsoleOptionsFrontendPersisterImpl.class).in(Singleton.class);
    }

}
