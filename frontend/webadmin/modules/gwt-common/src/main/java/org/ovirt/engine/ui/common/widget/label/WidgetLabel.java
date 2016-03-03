package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;

/**
 * This interface is intended to be implemented by labels that can follow state of their target widgets.
 * I.e.
 * <ul>
 *     <li>enabled/disable state + hints</li>
 *     <li>element id</li>
 * </ul>
 *
 * Such label can be declaratively attached to its target widget in *.ui.xml file
 * without the need of code co-location.
 *
 * <pre>
 * {@code
 *     <LabelWithTooltip forWidget="{intEditor}" />
 *     ...
 *     <IntegerEntityModelEditor ui:field="intEditor" />
 * }
 * </pre>
 *
 * @see HasWidgetLabels
 */
public interface WidgetLabel extends HasEnabledWithHints {

    /**
     * Setter of target widget.
     * <br/>
     * Intended to be called declaratively from ui.xml files
     */
    void setForWidget(HasWidgetLabels targetWidget);

    /**
     * HTML Label "for" attribute setter
     * <br/>
     * Intended to be called by implementations of {@link HasWidgetLabels}
     */
    void setFor(final String targetId);
}
