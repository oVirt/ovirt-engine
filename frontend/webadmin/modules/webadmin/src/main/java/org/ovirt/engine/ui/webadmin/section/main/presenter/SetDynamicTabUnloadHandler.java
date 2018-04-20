package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.core.client.JavaScriptObject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when {@link DynamicUrlContentTabPresenter} should set an unload handler.
 */
@GenEvent
public class SetDynamicTabUnloadHandler {

    @Order(1)
    String historyToken;

    @Order(2)
    JavaScriptObject unloadHandler;

}
