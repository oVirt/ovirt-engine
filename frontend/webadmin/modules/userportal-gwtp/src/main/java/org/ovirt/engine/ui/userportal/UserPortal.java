package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.EntryPoint;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

/**
 * UserPortal application entry point.
 */
public class UserPortal implements EntryPoint {

    @Override
    public void onModuleLoad() {
        ClientGinjector ginjector = ClientGinjectorProvider.instance();

        DelayedBindRegistry.bind(ginjector);
        ginjector.getPlaceManager().revealCurrentPlace();
    }

}
