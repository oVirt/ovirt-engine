package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.TooltipPanel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;

public class LabelWithToolTip extends HTML {

    private final TooltipPanel tooltipPanel = new TooltipPanel(true, this);

    public LabelWithToolTip() {
        this("", -1); //$NON-NLS-1$
    }

    public LabelWithToolTip(String text) {
        this(text, -1);
    }

    public LabelWithToolTip(SafeHtml text) {
        this(text.asString());
    }

    public LabelWithToolTip(String text, int length) {
        super(text);

        if (length > -1 && text.length() > length) {
            setText(text.substring(0, length - 3) + "..."); //$NON-NLS-1$
        }
        setTitle(text);
    }

    @Override
    public void setTitle(String text) {
        tooltipPanel.setText(text);
    }

    public void setTitle(SafeHtml text) {
        tooltipPanel.setText(text);
    }
}
