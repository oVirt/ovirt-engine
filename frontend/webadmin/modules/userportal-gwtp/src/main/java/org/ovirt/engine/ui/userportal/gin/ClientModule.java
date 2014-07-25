package org.ovirt.engine.ui.userportal.gin;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * UserPortal application GIN module.
 */
public class ClientModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new SystemModule());
        install(new UiCommonModule());
        install(new PresenterModule());
        install(new UtilsModule());
    }

}
