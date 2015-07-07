package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link AbstractTabPresenter} should add new action button to its view.
 */
@GenEvent
public class AddTabActionButton {

    String historyToken;

    ActionButtonDefinition<?> buttonDefinition;

}
