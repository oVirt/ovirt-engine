package org.ovirt.engine.ui.webadmin.gin;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * WebAdmin application GIN module.
 */
public class ClientModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new SystemModule());
        install(new UiCommonModule());
        install(new PresenterModule());
        install(new PluginModule());
        install(new UtilsModule());
    }

}
