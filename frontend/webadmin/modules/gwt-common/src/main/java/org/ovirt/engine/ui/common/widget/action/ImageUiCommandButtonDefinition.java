package org.ovirt.engine.ui.common.widget.action;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.shared.EventBus;

/**
 * UiCommon {@linkplain org.ovirt.engine.ui.uicommonweb.UICommand command} button definition that has an image
 * associated with it.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class ImageUiCommandButtonDefinition<T> extends UiCommandButtonDefinition<T> {

    private HasCssName icon;

    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title,
            HasCssName icon,
            boolean showTitle,
            boolean imageAfterTitle, CommandLocation commandLocation) {
        super(eventBus, title, commandLocation);
        this.icon = icon;
    }

    /**
     * Creates a new button with the given title and images.
     *
     * @param title
     *            The Command Text title
     * @param enabledImage
     *            The Image to display when the command is Enabled
     * @param disabledImage
     *            The Image to display when the command is Disabled
     */
    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title,
            HasCssName icon,
            boolean showTitle,
            boolean imageAfterTitle) {
        this(eventBus,
                title,
                icon,
                showTitle,
                imageAfterTitle,
                CommandLocation.ContextAndToolBar);
    }

    public ImageUiCommandButtonDefinition(EventBus eventBus,
            String title, HasCssName icon) {
        this(eventBus, title, icon, false, false);
    }

    protected ImageUiCommandButtonDefinition(EventBus eventBus, String title, CommandLocation commandLocation) {
        super(eventBus, title, commandLocation);
    }

    @Override
    public HasCssName getIcon() {
        return icon;
    }
}
