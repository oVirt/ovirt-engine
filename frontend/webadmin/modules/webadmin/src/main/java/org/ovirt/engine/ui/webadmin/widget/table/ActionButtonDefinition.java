package org.ovirt.engine.ui.webadmin.widget.table;

import java.util.List;

import org.ovirt.engine.ui.webadmin.widget.HasAccess;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Describes a button rendered within an {@link AbstractActionTable}'s action panel.
 * 
 * @param <T>
 *            Table row data type.
 */
public interface ActionButtonDefinition<T> extends HasAccess {

    /**
     * Action button click event callback.
     * 
     * @param selectedItems
     *            Items currently selected in the {@link AbstractActionTable}.
     */
    void onClick(List<T> selectedItems);

    /**
     * Checks whether or not this action button should be enabled for the given table selection.
     * 
     * @param selectedItems
     *            Items currently selected in the {@link AbstractActionTable}.
     */
    boolean isEnabled(List<T> selectedItems);

    /**
     * Indicate whether the action is implemented or not, This is only relevant for the first tech-preview of webadmin
     * where not all buttons may be implemented TODO: This is temporary and should be cleaned up when WebAdmin will be
     * fully implemented!
     * 
     * @param isImplemented
     *            whether this action is available or not
     * @return whether action is available or not
     */
    public boolean isImplemented();

    /**
     * If action is not available, then this property indicates whether the action is available in user portal or not
     * This is only affecting the message that will be displaying when the button is clicked. TODO: This is temporary
     * and should be cleaned up when WebAdmin will be fully implemented!
     * 
     * @param isImplInUserPortal
     *            whether this action is implemented in user portal or not
     * @return true/false
     */
    public boolean isImplInUserPortal();

    /**
     * Get the SafeHtml content to show when the Button is Enabled
     * 
     * @return
     */
    SafeHtml getEnabledHtml();

    /**
     * Get the SafeHtml content to show when the Button is Disabled
     * 
     * @return
     */
    SafeHtml getDisabledHtml();

    /**
     * Get the Button Tooltip Title (also used in Context Menus)
     * 
     * @return
     */
    String getTitle();

    /**
     * Indicate whether the action is available only from context menu (right-click)
     * 
     * @param isAvailableOnlyFromContext
     *            whether this action is available or not
     */
    public boolean isAvailableOnlyFromContext();
}
