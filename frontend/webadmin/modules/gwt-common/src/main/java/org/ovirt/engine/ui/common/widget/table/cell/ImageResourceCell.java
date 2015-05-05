package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Cell that renders an ImageResource. Supports setting a style / class. Supports tooltips.
 */
public class ImageResourceCell extends AbstractImageCell<ImageResource>  {

    @Override
    protected SafeHtml getRenderedImage(ImageResource value) {
        return AbstractImagePrototype.create(value).getSafeHtml();
    }
}
