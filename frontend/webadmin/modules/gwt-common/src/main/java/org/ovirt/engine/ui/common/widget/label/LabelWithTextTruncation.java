package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.utils.ElementUtils;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;

/**
 * A Label that supports text truncation. If the entire Label content doesn't fit in the space allotted,
 * the text is truncated, and an ellipse (...) is appended to the string. The full text will be rendered
 * in a tooltip.
 */
public class LabelWithTextTruncation extends Composite implements IsEditor<LeafValueEditor<String>>, HasEnabled {

    WidgetTooltip tooltip;
    private Label label;
    private boolean enabled = true;

    public LabelWithTextTruncation() {
        label = new Label();
        tooltip = new WidgetTooltip(label);
        initWidget(tooltip.asWidget());
        getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        getElement().getStyle().setOverflow(Overflow.HIDDEN);
        getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
    }

    public LabelWithTextTruncation(String text) {
        this();
        setText(text);
    }

    public void setText(String text) {
        label.setText(text);
        refreshTooltip();
    }

    public String getText() {
        return label.getText();
    }

    private void refreshTooltip() {
        if (!isAttached() || !isVisible()) {
            return;
        }

        if (ElementUtils.detectHorizontalOverflow(label.getElement())) {
            tooltip.setText(getText());
        } else {
            tooltip.setText(null);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        refreshTooltip();
    }

    @Override
    public LeafValueEditor<String> asEditor() {
        return label.asEditor();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getElement().getStyle().setColor(enabled ? "#333333" : "gray"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
