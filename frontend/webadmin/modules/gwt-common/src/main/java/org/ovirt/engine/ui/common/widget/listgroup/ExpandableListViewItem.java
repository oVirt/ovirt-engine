package org.ovirt.engine.ui.common.widget.listgroup;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

public class ExpandableListViewItem extends FlowPanel implements HasClickHandlers {
    public static final String HIDDEN = "hidden"; // $NON-NLS-1$
    private static final String FA_ANGLE_RIGHT = "fa-angle-right"; // $NON-NLS-1$
    private static final String FA_ANGLE_DOWN = "fa-angle-down"; // $NON-NLS-1$

    Span caretIcon;

    Container details;

    public ExpandableListViewItem(String label, String iconCss) {
        addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND);
        caretIcon = new Span();
        caretIcon.addStyleName(Styles.FONT_AWESOME_BASE);
        caretIcon.addStyleName(FA_ANGLE_RIGHT);
        add(caretIcon);
        setIcon(iconCss);
        addLabel(label);
    }

    private String getBaseStyle(String iconCss) {
        String result = Styles.FONT_AWESOME_BASE;
        if (iconCss.startsWith(PatternflyConstants.PFICON)) {
            result = PatternflyConstants.PFICON;
        }
        return result;
    }

    public void setDetails(Container details) {
        this.details = details;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    private void addLabel(String label) {
        Span labelSpan = new Span();
        labelSpan.getElement().setInnerText(label);
        add(labelSpan);
    }

    private void setIcon(String iconCss) {
        Span iconPanel = new Span();
        iconPanel.addStyleName(getBaseStyle(iconCss));
        iconPanel.addStyleName(iconCss);
        add(iconPanel);
    }

    public boolean isActive() {
        return getStyleName().contains(Styles.ACTIVE);
    }

    public void toggleExpanded(boolean expand) {
        if (!expand) {
            removeStyleName(Styles.ACTIVE);
            caretIcon.removeStyleName(FA_ANGLE_DOWN);
            if (details != null) {
                details.addStyleName(HIDDEN);
            }
        } else {
            addStyleName(Styles.ACTIVE);
            caretIcon.addStyleName(FA_ANGLE_DOWN);
            if (details != null) {
                details.removeStyleName(HIDDEN);
            }
        }
    }

    public Container getDetails() {
        return details;
    }
}
