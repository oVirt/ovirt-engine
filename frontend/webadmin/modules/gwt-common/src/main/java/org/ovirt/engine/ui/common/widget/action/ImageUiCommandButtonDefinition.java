package org.ovirt.engine.ui.common.widget.action;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * UiCommon {@linkplain org.ovirt.engine.ui.uicommonweb.UICommand command} button definition that has an image
 * associated with it.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class ImageUiCommandButtonDefinition<T> extends UiCommandButtonDefinition<T> {

    private final SafeHtml enabledImage;
    private final SafeHtml disabledImage;
    private boolean showTitle;
    private boolean imageAfterTitle;

    public ImageUiCommandButtonDefinition(String title,
            ImageResource enabledImage,
            ImageResource disabledImage,
            boolean showTitle,
            boolean imageAfterTitle, boolean availableOnlyFromContext) {
        super(title, availableOnlyFromContext);
        this.enabledImage = enabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML()) : null;
        this.disabledImage = disabledImage != null
                ? SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML()) : null;
        this.showTitle = showTitle;
        this.imageAfterTitle = imageAfterTitle;
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
    public ImageUiCommandButtonDefinition(String title,
            ImageResource enabledImage,
            ImageResource disabledImage,
            boolean showTitle,
            boolean imageAfterTitle) {
        this(title, enabledImage, disabledImage, showTitle, imageAfterTitle, false);
    }

    public ImageUiCommandButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        this(title, enabledImage, disabledImage, false, false);
    }

    protected abstract CommonApplicationTemplates getCommonApplicationTemplates();

    @Override
    public SafeHtml getEnabledHtml() {
        return !showTitle ? enabledImage
                : (!imageAfterTitle ? getCommonApplicationTemplates().imageTextButton(enabledImage, getTitle())
                        : getCommonApplicationTemplates().textImageButton(getTitle(), enabledImage));
    }

    @Override
    public SafeHtml getDisabledHtml() {
        return !showTitle ? disabledImage : (!imageAfterTitle ? getCommonApplicationTemplates().imageTextButton(disabledImage, getTitle())
                : getCommonApplicationTemplates().textImageButton(getTitle(), disabledImage));
    }

}
