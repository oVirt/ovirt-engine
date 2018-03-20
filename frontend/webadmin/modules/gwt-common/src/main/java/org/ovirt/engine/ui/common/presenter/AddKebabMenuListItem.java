package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link ActionPanelPresenter} should add new menu item to its more menu.
 */
@GenEvent
public class AddKebabMenuListItem {

    String historyToken;

    ActionButtonDefinition<?> buttonDefinition;

}
