package org.ovirt.engine.ui.webadmin.widget.tab;

import com.google.gwt.dom.client.Style.HasCssName;

public interface PrimaryMenuItem {
    /**
     * Get the title of the menu item.
     * @return A string containing the title.
     */
    String getTitle();

    /**
     * Get the index of the menu item.
     * @return The index.
     */
    int getIndex();

    /**
     * Get the href of the menu item.
     * @return A string containing the href.
     */
    String getHref();

    /**
     * Get the css name of the icon for the menu item.
     * @return The css name as an HasCssName object.
     */
    HasCssName getIcon();
}
