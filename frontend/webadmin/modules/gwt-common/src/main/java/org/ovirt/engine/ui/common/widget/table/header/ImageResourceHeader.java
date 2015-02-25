package org.ovirt.engine.ui.common.widget.table.header;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Header for rendering {@link ImageResource}s. Supports tooltips.
 */
public class ImageResourceHeader extends SafeHtmlHeader {

    private final static CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    private ImageResource headerImage;

    public ImageResourceHeader(ImageResource image, SafeHtml tooltipText) {
        super(SafeHtmlUtils.fromSafeConstant(""), tooltipText); //$NON-NLS-1$
        if (image != null) {
            this.headerImage = image;
            setValue(getHeaderHtml());
        }
    }

    public ImageResourceHeader(ImageResource image) {
        super(SafeHtmlUtils.fromSafeConstant("")); //$NON-NLS-1$
    }

    @Override
    public SafeHtmlCell getCell() {
        return (SafeHtmlCell) super.getCell();
    }

    protected SafeHtml getHeaderHtml() {
        if (headerImage == null) {
            return SafeHtmlUtils.fromSafeConstant(constants.empty());
        }

        return templates.headerImage(SafeHtmlUtils.fromTrustedString(
                AbstractImagePrototype.create(headerImage).getHTML()));
    }

    protected ImageResource getHeaderImage() {
        return headerImage;
    }

    protected void setHeaderImage(ImageResource headerImage) {
        this.headerImage = headerImage;
    }

}
