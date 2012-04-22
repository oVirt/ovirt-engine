package org.ovirt.engine.ui.common.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.MenuItemSeparator;

public class TitleMenuItemSeparator extends MenuItemSeparator {

    public TitleMenuItemSeparator(String title) {
        super();
        DOM.setInnerHTML(getElement(), "<b>" + title + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
        setStyleName("gwt-MenuItem"); //$NON-NLS-1$
    }

    public TitleMenuItemSeparator(SafeHtml title) {
        this(title.asString());
    }
}
