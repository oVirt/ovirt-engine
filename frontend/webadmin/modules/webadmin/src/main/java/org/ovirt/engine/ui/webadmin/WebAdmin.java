package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.EntryPoint;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

/**
 * WebAdmin application entry point.
 */
public class WebAdmin implements EntryPoint {

    @Override
    public void onModuleLoad() {
        ClientGinjector ginjector = ClientGinjectorProvider.instance();

        DelayedBindRegistry.bind(ginjector);
        ginjector.getPlaceManager().revealCurrentPlace();
    }

}
