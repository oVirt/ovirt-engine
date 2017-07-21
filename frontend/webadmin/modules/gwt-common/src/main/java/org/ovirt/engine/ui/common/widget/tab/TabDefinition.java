package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.widget.HasAccess;

import com.google.gwt.dom.client.Style.HasCssName;
import com.gwtplatform.mvp.client.Tab;

/**
 * Describes a tab rendered within a {@link com.gwtplatform.mvp.client.TabPanel TabPanel}.
 */
public interface TabDefinition extends Tab, HasAccess {

    String TAB_ID_PREFIX = "tab-id-"; //$NON-NLS-1$

    /**
     * Set the group title this tab belongs to
     * @param group The group title
     */
    void setGroupTitle(String groupTitle);

    /**
     * Get the group title this tab belongs to
     * @return The group title
     */
    String getGroupTitle();

    /**
     * Set the group priority
     * @param groupPriority The group priority
     */
    void setGroupPriority(int groupPriority);

    /**
     * Get the group priority
     * @return the group priority
     */
    int getGroupPriority();

    /**
     * Get the icon associated with the menu
     * @return The icon
     */
    HasCssName getIcon();

    /**
     * Set the id
     */
    void setId(String id);

    /**
     * Get the id
     */
    String getId();

}
