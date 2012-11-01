package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when {@link DynamicUrlContentTabPresenter} should update its content URL.
 */
@GenEvent
public class SetDynamicTabContentUrl {

    @Order(1)
    String historyToken;

    @Order(2)
    String contentUrl;

}
