package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

public class UiCommandLink extends AbstractUiCommandButton implements Focusable {

    interface WidgetUiBinder extends UiBinder<Widget, UiCommandLink> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Anchor anchor;

    @UiField
    WidgetTooltip tooltip;

    public UiCommandLink() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public UiCommandLink(String label) {
        setLabel(label);
    }

    @Override
    public int getTabIndex() {
        return anchor.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        anchor.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        anchor.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        anchor.setTabIndex(index);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    protected void updateButton() {
        super.updateButton();
        tooltip.setText(buildTooltipText());
        tooltip.reconfigure();
    }

    /**
     * Use prohibition reasons for tooltip
     */
    protected String buildTooltipText() {
        StringBuilder tooltipText = new StringBuilder();
        if (!getCommand().getExecuteProhibitionReasons().isEmpty()) {
            for (String reason: getCommand().getExecuteProhibitionReasons()) {
                if (tooltipText.length() == 0) {
                    tooltipText.append(", "); //$NON-NLS-1$
                }
                tooltipText.append(reason);
            }
        }
        return tooltipText.toString();
    }

    @Override
    protected Widget getButtonWidget() {
        return anchor;
    }
}
