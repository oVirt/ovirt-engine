package org.ovirt.engine.ui.common.widget.listgroup;

import java.util.List;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class ExpandableListViewItem extends FlowPanel implements HasClickHandlers {
    public static final String HIDDEN = "hidden"; // $NON-NLS-1$
    private static final String FA_ANGLE_RIGHT = "fa-angle-right"; // $NON-NLS-1$
    private static final String FA_ANGLE_DOWN = "fa-angle-down"; // $NON-NLS-1$

    Span caretIcon;

    Container details;

    public ExpandableListViewItem(SafeHtml label, List<IsWidget> icons) {
        this(label);
        if (icons != null) {
            for (IsWidget iconCss : icons) {
                add(iconCss);
            }
        }
    }

    public ExpandableListViewItem(SafeHtml label) {
        addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND);
        caretIcon = new Span();
        caretIcon.addStyleName(Styles.FONT_AWESOME_BASE);
        caretIcon.addStyleName(FA_ANGLE_RIGHT);
        add(caretIcon);
        addLabel(label);
    }

    public ExpandableListViewItem(SafeHtml label, String iconCssString) {
        this(label);
        if (iconCssString != null) {
            addIcon(iconCssString);
        }
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

    private void addLabel(SafeHtml label) {
        Span labelSpan = new Span();
        labelSpan.getElement().setInnerSafeHtml(label);
        add(labelSpan);
    }

    private void addIcon(String iconCss) {
        if (iconCss != null && !iconCss.isEmpty()) {
            Span iconPanel = new Span();
            iconPanel.addStyleName(getBaseStyle(iconCss));
            iconPanel.addStyleName(iconCss);
            iconPanel.addStyleName(PatternflyConstants.LIST_VIEW_ICON_PANEL);
            add(iconPanel);
        }
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
