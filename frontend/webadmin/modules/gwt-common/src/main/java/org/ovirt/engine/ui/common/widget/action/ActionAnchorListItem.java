package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.safehtml.shared.SafeHtml;

public class ActionAnchorListItem extends AnchorListItem implements ActionButton {

    public ActionAnchorListItem(String label) {
        super(label);
    }

    @Override
    public void setTooltip(SafeHtml setTooltipText) {
        createTooltip(setTooltipText);
    }

    private WidgetTooltip createTooltip(SafeHtml tooltipText) {
        WidgetTooltip toolTip = new WidgetTooltip(this);
        toolTip.setHtml(tooltipText);
        return toolTip;
    }

    @Override
    public void setTooltip(SafeHtml setTooltipText, Placement placement) {
        WidgetTooltip toolTip = createTooltip(setTooltipText);
        toolTip.setPlacement(placement);
    }
}
