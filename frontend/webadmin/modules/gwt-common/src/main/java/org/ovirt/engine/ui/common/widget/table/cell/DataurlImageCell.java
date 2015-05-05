package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class DataurlImageCell extends AbstractImageCell<String> {

    interface ImageTemplate extends SafeHtmlTemplates {
        @Template("<img src='{0}' />")
        SafeHtml image(SafeUri dataurl);
    }

    private ImageTemplate template = GWT.create(ImageTemplate.class);

    @Override
    protected SafeHtml getRenderedImage(String imageDataurl) {
        return template.image(UriUtils.fromTrustedString(imageDataurl));
    }
}
