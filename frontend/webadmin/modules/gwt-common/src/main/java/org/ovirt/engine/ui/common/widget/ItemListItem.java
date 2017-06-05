package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

public class ItemListItem extends ListItem {

    private final Anchor deactivationAnchor = new Anchor();

    public void init(String itemText) {
        setPaddingBottom(10);
        addLabelSpan(itemText);
        addDeactivationAnchor();
    }

    private void addLabelSpan(String itemText) {
        Span labelSpan = new Span();
        labelSpan.addStyleName(Styles.LABEL);
        labelSpan.addStyleName(PatternflyConstants.PF_LABEL_INFO);
        labelSpan.setText(itemText);
        add(labelSpan);
    }

    private void addDeactivationAnchor() {
        Span closeIconSpan = new Span();
        closeIconSpan.addStyleName(PatternflyConstants.PFICON);
        closeIconSpan.addStyleName(PatternflyConstants.PFICON_CLOSE);

        deactivationAnchor.add(closeIconSpan);
        add(deactivationAnchor);
    }

    public Anchor getDeactivationAnchor() {
        return deactivationAnchor;
    }
}
