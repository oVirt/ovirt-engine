package org.ovirt.engine.ui.common.presenter;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when {@link DynamicTabPresenter} should be updated with regard to its tab/place accessibility.
 */
@GenEvent
public class SetDynamicTabAccessible {

    @Order(1)
    String historyToken;

    @Order(2)
    boolean tabAccessible;

}
