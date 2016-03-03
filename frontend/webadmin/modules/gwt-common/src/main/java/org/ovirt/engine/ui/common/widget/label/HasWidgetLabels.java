package org.ovirt.engine.ui.common.widget.label;

/**
 * This interface is intended to be implemented by widgets that can be labeled by {@link WidgetLabel}.
 * <p>
 *     Implementing widgets should keep track of added labels and let them know about state changes
 *     - changes of id, enable/disable state.
 * </p>
 *
 * @see WidgetLabel
 */
public interface HasWidgetLabels {

    void addLabel(WidgetLabel label);
    void removeLabel(WidgetLabel label);
}
