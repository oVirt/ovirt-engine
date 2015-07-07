package org.ovirt.engine.ui.userportal.widget.extended;

import org.ovirt.engine.ui.userportal.widget.UserPortalSplitLayoutPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Element;

public class ExtendedViewSplitLayoutPanel extends UserPortalSplitLayoutPanel {

    interface SplitterTemplate extends SafeHtmlTemplates {
        @Template(
                "<div style=\"background-color: #ffffff; height: 5px; position: absolute; left: 0px; right: 0px; top: 0px; bottom: 0px \" />"
                +
                "<div style=\"background-color: #dde4ea; height: 4px; position: absolute; left: 2px; right: 2px; top: 0px; bottom: 1px \" />"
                +
                "<div style=\"background-color: #b7c5d1; height: 3px; position: absolute; left: 0px; right: 0px; top: 0px; bottom: 2px \" />"
                +
                "<div style=\"background-color: #3a5f7c; height: 2px; position: absolute; left: 0px; top: 0px; right: 0px; bottom: 3px\">{0}</div>")
        SafeHtml dragger(SafeHtml middleImage);
    }

    private static final SplitterTemplate template = GWT.create(SplitterTemplate.class);

    private final SafeHtml middleBackgroundImage;

    public ExtendedViewSplitLayoutPanel(ImageResource middleBackgroundImage) {
        super(4);

        this.middleBackgroundImage =
                createImageWithStyle(middleBackgroundImage, "position: absolute; left: " + 50 + "%; top: " + 1 + "px;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void init() {
        Element dragger = findElementByClassName(this.getElement(), "gwt-SplitLayoutPanel-VDragger"); //$NON-NLS-1$
        if (dragger != null) {
            dragger.setInnerHTML(
                    template.dragger(
                            middleBackgroundImage).asString()
                    );
        }
    }

}
