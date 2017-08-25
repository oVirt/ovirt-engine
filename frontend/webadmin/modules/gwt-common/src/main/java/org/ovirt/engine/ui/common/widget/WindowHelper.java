package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;

public class WindowHelper {

    /**
     * Determine the thickness of a scroll-bar if it was visible. Assumes width of vertical scrollbar and height
     * of a horizontal scrollbar are the same.
     * @return The height in PX.
     */
    public static int determineScrollbarThickness() {
        Element panel = DOM.createDiv();
        panel.getStyle().setWidth(100, Unit.PX);
        panel.getStyle().setHeight(100, Unit.PX);
        panel.getStyle().setOverflow(Overflow.SCROLL);
        Document.get().getBody().appendChild(panel);
        int scrollbarHeight = panel.getOffsetHeight() - panel.getClientHeight();
        Document.get().getBody().removeChild(panel);
        return scrollbarHeight;
    }

}
