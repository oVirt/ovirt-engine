package org.ovirt.engine.ui.common.view.popup;

public interface FocusableComponentsContainer {

    /**
     * This method should be implemented in each component that is presented
     * in dialog and contains sub components that can gain focus. it sets
     * the 'tab index' for the sub components in order to ensure the
     * right order of focus traversal in dialogs.
     *
     * This method is called is a recursive manner: it gets the next index
     * to be set (in accordance with the previous settings in the dialog),
     * and it should return the next index to be set for other components in
     * the dialog.
     *
     * @param nextTabIndex the next tab index to be set for components contained in this container
     * @return the next tab index to be set for other components in the dialog
     */
    public int setTabIndexes(int nextTabIndex);
}
