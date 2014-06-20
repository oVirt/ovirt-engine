package org.ovirt.engine.ui.common.widget;

import java.util.List;

/**
 * Widgets that implement this interface can be visually marked as valid or invalid.
 */
public interface HasValidation {

    /**
     * Marks this widget as valid.
     */
    void markAsValid();

    /**
     * Marks this widget as invalid, providing an optional list of validation hints.
     *
     * @param validationHints
     *            Validation hints describing reasons why this widget is invalid (can be empty or {@code null}).
     */
    void markAsInvalid(List<String> validationHints);

    /**
     * Returns true/false according to the markAsValid/markAsInvalid
     */
    boolean isValid();
}
