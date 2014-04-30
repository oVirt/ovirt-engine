package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.TooltipPanel;

import com.google.gwt.user.client.ui.Label;

public class LabelWithToolTip extends Label {

    private final TooltipPanel tooltipPanel = new TooltipPanel(true, this);

    public LabelWithToolTip() {
        this("", -1); //$NON-NLS-1$
    }

    public LabelWithToolTip(String text, int length) {
        super(text);

        if (length > -1 && text.length() > length) {
            setText(text.substring(0, length - 3) + "..."); //$NON-NLS-1$
        }
        setTitle(text);
    }

    public LabelWithToolTip(String text) {
        this(text, -1);
    }

    @Override
    public void setTitle(String text) {
        tooltipPanel.setText(text);
    }
}
