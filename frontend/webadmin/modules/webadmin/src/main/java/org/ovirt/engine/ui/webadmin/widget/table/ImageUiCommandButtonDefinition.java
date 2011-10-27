package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A Button Definition for UICommands that have an Image
 * 
 * @param <T>
 */
public class ImageUiCommandButtonDefinition<T> extends UiCommandButtonDefinition<T> {
    private final SafeHtml enabledImage;
    private final SafeHtml disabledImage;

    /**
     * create a new UICommand button, with the provided title and images.
     * 
     * @param command
     *            The UiCommand
     * @param title
     *            The Command Text title
     * @param enabledImage
     *            The Image to display when the command is Enabled
     * @param disabledImage
     *            The Image to display when the command is Disabled
     */
    public ImageUiCommandButtonDefinition(UICommand command,
            String title,
            ImageResource enabledImage,
            ImageResource disabledImage) {
        super(command, title);
        this.enabledImage =
                enabledImage != null ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage)
                        .getHTML()) : null;
        this.disabledImage =
                disabledImage != null ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage)
                        .getHTML()) : null;
    }

    @Override
    public SafeHtml getDisabledHtml() {
        return disabledImage;
    }

    @Override
    public SafeHtml getEnabledHtml() {
        return enabledImage;
    }
}
