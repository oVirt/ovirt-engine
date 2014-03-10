package org.ovirt.engine.ui.common.widget;

/**
 * Widgets that implement this interface have a visual representation which shows this widget attached or detached to something
 */
public interface HasDetachable {

    /**
     * This widget shows some icon which means if this widget is attached to something or not.
     * This method hides/shows this icon
     */
    void setDetachableIconVisible(boolean visible);

    /**
     * Changes the icon to attached/detached
     */
    void setAttached(boolean attached);

}
