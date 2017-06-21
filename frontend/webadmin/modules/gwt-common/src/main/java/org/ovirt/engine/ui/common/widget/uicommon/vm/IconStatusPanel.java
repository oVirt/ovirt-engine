package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.dom.client.Style.HasCssName;

public class IconStatusPanel extends Span {

    private static final String OBRAND_LIST_VIEW_ICON = "obrand_list_view_icon"; // $NON-NLS-1$

    public IconStatusPanel(HasCssName iconCssName) {
        this(iconCssName, Styles.FONT_AWESOME_BASE);
    }
    public IconStatusPanel(HasCssName iconCssName, String base) {
        this(iconCssName.getCssName(), base);
    }

    public IconStatusPanel(String iconCssName, String base) {
        Span icon = new Span();
        Italic iconContainer = new Italic();
        iconContainer.addStyleName(base);
        iconContainer.addStyleName(OBRAND_LIST_VIEW_ICON);
        icon.add(iconContainer);
        iconContainer.addStyleName(iconCssName);
        add(icon);
    }

    public void setIconColor(String color) {
        getElement().getStyle().setBackgroundColor(color);
    }
}
