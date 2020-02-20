package org.ovirt.engine.ui.common.widget.action;

import static java.util.Collections.emptyList;

import java.util.List;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.shared.EventBus;

/**
 * UiCommon {@linkplain org.ovirt.engine.ui.uicommonweb.UICommand command} button definition that has an image
 * associated with it.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class ImageUiCommandButtonDefinition<E, T> extends UiCommandButtonDefinition<E, T> {

    private HasCssName icon;

    /**
     * Creates a new button with the given title and images.
     *
     * @param title
     *            The Command Text title
     */
    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title,
            HasCssName icon,
            boolean showTitle,
            boolean imageAfterTitle) {
        this(eventBus, title, icon, showTitle, imageAfterTitle, emptyList());
    }

    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title,
            HasCssName icon) {
        this(eventBus, title, icon, false, false);
    }

    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title,
            HasCssName icon,
            boolean showTitle,
            boolean imageAfterTitle,
            List<ActionButtonDefinition<E, T>> subActions) {
        super(eventBus, title, false, subActions);
        this.icon = icon;
    }

    @Override
    public HasCssName getIcon() {
        return icon;
    }
}
