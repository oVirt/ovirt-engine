package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.utils.ElementUtils;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;

/*
 * In case the label's text is cropped, the text is visually trimmed. To let the user know that the text was trimmed
 * '...' (three dots) are appended to the string. The tooltip contains the full text
 * string.
 */
public class LabelWithTextOverflow extends Composite implements IsEditor<LeafValueEditor<String>>, HasEnabled {

    private Label label;
    private boolean enabled = true;

    public LabelWithTextOverflow() {
        label = new Label();
        initWidget(label);
        getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        getElement().getStyle().setOverflow(Overflow.HIDDEN);
        getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
    }

    public LabelWithTextOverflow(String text) {
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

        if (ElementUtils.detectOverflowUsingScrollWidth(label.getElement())) {
            label.setTitle(getText());
        } else {
            label.setTitle(null);
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
        getElement().getStyle().setColor(enabled ? "#333333" : "gray"); //$NON-NLS-1$ $NON-NLS-2$
    }
}
