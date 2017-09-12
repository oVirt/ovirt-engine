package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LabelWithTooltip implements WidgetLabel, IsWidget {

    private HasWidgetLabels currentTarget;
    private final WidgetTooltip tooltip;
    private final EnableableFormLabel label;

    public LabelWithTooltip() {
        label = new EnableableFormLabel();
        tooltip = new WidgetTooltip(label);
    }

    @Override
    public void setForWidget(HasWidgetLabels targetWidget) {
        if (currentTarget != null) {
            currentTarget.removeLabel(this);
        }
        currentTarget = targetWidget;
        currentTarget.addLabel(this);
    }

    @Override
    public void setFor(String targetId) {
        label.setFor(targetId);
    }

    @Override
    public void disable(String disabilityHint) {
        tooltip.setText(disabilityHint);
        setEnabled(false);
    }

    @Override
    public boolean isEnabled() {
        return label.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        if (enabled) {
            tooltip.setText("");
        }
    }

    public void setText(String text) {
        label.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void setTooltip(String tooltipText) {
        tooltip.setText(tooltipText);
    }

    @Override
    public Widget asWidget() {
        return tooltip.asWidget();
    }

    public void addStyleName(String style) {
        label.addStyleName(style);
    }

    public void setAddStyleName(String style) {
        label.addStyleName(style);
    }

    public void setStyleName(String style) {
        label.setStyleName(style);
    }

    public void setStyleName(String style, boolean add) {
        label.setStyleName(style, add);
    }

    public void removeStyleName(String style) {
        label.removeStyleName(style);
    }
}
