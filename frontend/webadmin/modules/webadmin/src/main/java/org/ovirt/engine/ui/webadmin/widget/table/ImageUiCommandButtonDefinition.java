package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * {@link UICommand} button definition that has an image associated with it.
 * 
 * @param <T>
 *            Table row data type.
 */
public abstract class ImageUiCommandButtonDefinition<T> extends UiCommandButtonDefinition<T> {

    private final SafeHtml enabledImage;
    private final SafeHtml disabledImage;

    /**
     * Creates a new button with the given title and images.
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
    public ImageUiCommandButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        super(title);
        this.enabledImage = enabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML()) : null;
        this.disabledImage = disabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML()) : null;
    }

    @Override
    public SafeHtml getEnabledHtml() {
        return enabledImage;
    }

    @Override
    public SafeHtml getDisabledHtml() {
        return disabledImage;
    }

}
