package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * Event triggered when {@link ActionPanelPresenter} should add new button to the action panel.
 */
@GenEvent
public class AddActionButton {

    @Order(1)
    String historyToken;

    @Order(2)
    ActionButtonDefinition<?, ?> buttonDefinition;

    /**
     * If {@code true}, the button will be represented as a menu item within the kebab menu.
     */
    @Order(3)
    boolean addToKebabMenu;

}
