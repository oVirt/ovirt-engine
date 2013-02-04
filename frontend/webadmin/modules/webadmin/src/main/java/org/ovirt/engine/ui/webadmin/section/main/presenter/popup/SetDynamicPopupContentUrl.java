package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when {@link DynamicUrlContentPopupPresenterWidget} should update its content URL.
 */
@GenEvent
public class SetDynamicPopupContentUrl {

    @Order(1)
    String dialogToken;

    @Order(2)
    String contentUrl;

}
