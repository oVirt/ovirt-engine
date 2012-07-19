package org.ovirt.engine.ui.common.widget;

import com.google.gwt.user.client.ui.HasEnabled;

/**
 * Extends the {@link HasEnabled} interface, allow to disable a specific item in a container widget
 */
public interface HasEnabledForContainter<T> extends HasEnabled {

    void setEnabled(T value, boolean enabled);

}
