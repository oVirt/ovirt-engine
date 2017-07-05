package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

/**
 * Represents an action panel widget.
 *
 * @param <T>
 *            Action panel item type.
 */
public interface ActionPanel<T> {

    /**
     * Adds a new button to the action panel.
     */
    void addActionButton(ActionButtonDefinition<T> buttonDef);

    /**
     * Creates a menu item from the button definition
     * @param buttonDef The button definition
     * @return Am {@code AnchorListItem} that implements ActionButton.
     */
    void addMenuListItem(ActionButtonDefinition<T> buttonDef);

    /**
     * Returns items currently selected in this action panel, or {@code null} if this action panel does not permit item
     * selection.
     */
    List<T> getSelectedItems();

}
