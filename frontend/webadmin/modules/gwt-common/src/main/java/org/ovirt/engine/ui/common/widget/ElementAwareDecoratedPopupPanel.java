package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

/**
 * DecoratedPopupPanel that can be positioned below and left-aligned with any Element.
 * (Standard DecoratedPopupPanel only works with UIObjects.)
 *
 */
public class ElementAwareDecoratedPopupPanel extends DecoratedPopupPanel {

    public void showRelativeTo(final Element target) {
        setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = target.getAbsoluteLeft();
                int top = target.getAbsoluteTop() + target.getOffsetHeight();

                setPopupPosition(left, top);
            }
        });
    }
}
