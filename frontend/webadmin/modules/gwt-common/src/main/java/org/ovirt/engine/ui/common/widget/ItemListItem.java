package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.dom.client.Style;

public class ItemListItem extends ListItem {

    private final Anchor deactivationAnchor = new Anchor();

    public void init(String itemText) {
        Span labelSpan = new Span();
        labelSpan.addStyleName(Styles.LABEL);
        labelSpan.addStyleName("label-info"); // $NON-NLS-1$
        labelSpan.setText(itemText);

        initDeactivationAnchor();
        labelSpan.add(deactivationAnchor);

        setPaddingLeft(0);
        setMarginBottom(10);
        add(labelSpan);
    }

    private void initDeactivationAnchor() {
        Span closeIconSpan = new Span();
        closeIconSpan.addStyleName(PatternflyConstants.PFICON);
        closeIconSpan.addStyleName("pficon-close"); // $NON-NLS-1$
        closeIconSpan.getElement().getStyle().setColor("white"); // $NON-NLS-1$
        closeIconSpan.setMarginLeft(5);
        closeIconSpan.setPaddingBottom(2);
        closeIconSpan.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        deactivationAnchor.add(closeIconSpan);
    }

    public Anchor getDeactivationAnchor() {
        return deactivationAnchor;
    }
}
