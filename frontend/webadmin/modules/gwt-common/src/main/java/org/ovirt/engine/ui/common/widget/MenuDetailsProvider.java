package org.ovirt.engine.ui.common.widget;

/**
 * Provide details about the menu to interested parties.
 */
public interface MenuDetailsProvider {
    /**
     * Get the label associated with the passed in href
     * @param href The href to use for looking up the label.
     * @return The label associated with the href.
     */
    String getLabelFromHref(String href);

    /**
     * Set the primary menu active based on the passed in href.
     * @param href The href to use to set active primary menu.
     */
    void setMenuActive(String href);

}
