package org.ovirt.engine.ui.webadmin.widget.action;

import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * {@link org.ovirt.engine.ui.uicommonweb.UICommand} button definition that has an image associated with it.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class ImageUiCommandButtonDefinition<T> extends UiCommandButtonDefinition<T> {

    private static final ApplicationTemplates templates = ClientGinjectorProvider.instance().getApplicationTemplates();

    private final SafeHtml enabledImage;
    private final SafeHtml disabledImage;
    private boolean showTitle;

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
    public ImageUiCommandButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        super(title);
        this.enabledImage = enabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML()) : null;
        this.disabledImage = disabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML()) : null;
    }

    public ImageUiCommandButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage,
            boolean showTitle) {
        this(title, enabledImage, disabledImage);
        this.showTitle = showTitle;
    }

    @Override
    public SafeHtml getEnabledHtml() {
        return !showTitle ? enabledImage : templates.imageTextButton(enabledImage, getTitle());
    }

    @Override
    public SafeHtml getDisabledHtml() {
        return !showTitle ? disabledImage : templates.imageTextButton(disabledImage, getTitle());
    }

}
