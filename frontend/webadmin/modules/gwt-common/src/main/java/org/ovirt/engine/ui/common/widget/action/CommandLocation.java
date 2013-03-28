package org.ovirt.engine.ui.common.widget.action;

/**
 * Enumerates possible locations of an action button within the user interface.
 */
public enum CommandLocation {

    /**
     * Action button available only from context menu.
     */
    OnlyFromContext,

    /**
     * Action button available only from toolbar (action panel).
     */
    OnlyFromToolBar,

    /**
     * Action button available from both context menu and toolbar (action panel).
     */
    ContextAndToolBar;

}
