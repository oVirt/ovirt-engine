package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LabelWithTooltip implements WidgetLabel, IsWidget {

    private HasWidgetLabels currentTarget;
    private final WidgetTooltip tooltip;

    public LabelWithTooltip() {
        tooltip = new WidgetTooltip(new EnableableFormLabel());
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
        getLabel().setFor(targetId);
    }

    @Override
    public void disable(String disabilityHint) {
        tooltip.setText(disabilityHint);
        tooltip.reconfigure();
        setEnabled(false);
    }

    @Override
    public boolean isEnabled() {
        return getLabel().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getLabel().setEnabled(enabled);
        if (enabled) {
            tooltip.setText("");
            tooltip.reconfigure();
        }
    }

    public void setText(String text) {
        getLabel().setText(text);
    }

    private EnableableFormLabel getLabel() {
        return (EnableableFormLabel) tooltip.getWidget();
    }

    @Override
    public Widget asWidget() {
        return tooltip.asWidget();
    }

    public void setAddStyleNames(String style) {
        getLabel().addStyleName(style);
    }
}
