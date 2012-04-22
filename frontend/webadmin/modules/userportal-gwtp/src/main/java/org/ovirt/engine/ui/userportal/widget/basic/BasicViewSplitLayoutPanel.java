package org.ovirt.engine.ui.userportal.widget.basic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class BasicViewSplitLayoutPanel extends SplitLayoutPanel {

    private static final int SPLILLER_WIDTH_PX = 4;

    private final SafeHtml topBackgroundImage;

    private final SafeHtml middleBackgroundImage;

    interface SplitterTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"width: {2}px; position: absolute; left: 0px; top: 0px; right: 0px; bottom: 2px\">{0}</div>"
                +
                "<div style=\"background-color: #3a5f7c; width: {2}px; position: absolute; left: 0px; top: 5px; right: 0px; bottom: 2px\">{1}</div>")
        SafeHtml dragger(SafeHtml topImage, SafeHtml middleImage, int width);
    }

    private static final SplitterTemplate template = GWT.create(SplitterTemplate.class);

    public BasicViewSplitLayoutPanel(ImageResource topBackgroundImage, ImageResource middleBackgroundImage) {
        super(SPLILLER_WIDTH_PX);

        this.topBackgroundImage =
                createImageWithStyle(topBackgroundImage, "position: absolute; top: " + 0 + "px; left: " + 0 + "px;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.middleBackgroundImage =
                createImageWithStyle(middleBackgroundImage, "position: absolute; top: " + 50 + "%; left: " + 1 + "px;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public void addWest(Widget widget, double size) {
        int width = Window.getClientWidth();
        // as it is not possible to set the width in ui.xml in percentage
        // it has to be set here
        super.addWest(widget, width * 0.6);
    }

    public void initWidget() {
        Element dragger = findElementByClassName(this.getElement(), "gwt-SplitLayoutPanel-HDragger"); //$NON-NLS-1$
        if (dragger != null) {
            dragger.setInnerHTML(
                    template.dragger(
                            topBackgroundImage,
                            middleBackgroundImage,
                            SPLILLER_WIDTH_PX).asString()
                    );
        }
    }

    private Element findElementByClassName(Element parent, String className) {
        int elementCount = DOM.getChildCount(parent);
        for (int i = 0; i < elementCount; i++) {
            Element e = DOM.getChild(parent, i);
            if (className.equals(e.getClassName())) {
                return e;
            }

            if (DOM.getChildCount(e) > 0) {
                Element res = findElementByClassName(e, className);
                if (res != null) {
                    return res;
                }
            }
        }

        return null;
    }

    private SafeHtml createImageWithStyle(ImageResource resource, String stylePrefix) {

        String style =
                stylePrefix + " width: " + resource.getWidth() //$NON-NLS-1$
                        + "px; height: " + resource.getHeight() //$NON-NLS-1$
                        + "px; background: url(" + resource.getURL() + ") no-repeat " + (-resource.getLeft() + "px ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        + (-resource.getTop() + "px"); //$NON-NLS-1$

        String clippedImgHtml = "<img " //$NON-NLS-1$
                + "onload='this.__gwtLastUnhandledEvent=\"load\";' src='" //$NON-NLS-1$
                + GWT.getModuleBaseURL() + "clear.cache.gif' style='" + style //$NON-NLS-1$
                + "' border='0'>"; //$NON-NLS-1$

        return SafeHtmlUtils.fromTrustedString(clippedImgHtml);
    }
}
