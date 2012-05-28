package org.ovirt.engine.ui.userportal.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class UserPortalSplitLayoutPanel extends SplitLayoutPanel {

    public UserPortalSplitLayoutPanel(int splitterSize) {
        super(splitterSize);
    }

    protected Element findElementByClassName(Element parent, String className) {
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

    protected SafeHtml createImageWithStyle(ImageResource resource, String stylePrefix) {

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
