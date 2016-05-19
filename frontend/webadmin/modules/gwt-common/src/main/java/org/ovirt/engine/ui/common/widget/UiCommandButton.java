package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class UiCommandButton extends AbstractUiCommandButton implements Focusable, HasEnabled {

    interface WidgetUiBinder extends UiBinder<Widget, UiCommandButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimpleDialogButton button;

    @UiField
    WidgetTooltip tooltip;

    public UiCommandButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public UiCommandButton(String label) {
        this(label, null);
    }

    public UiCommandButton(ImageResource image) {
        this("", image); //$NON-NLS-1$
    }

    public UiCommandButton(String label, ImageResource image) {
        this();
        setLabel(label);
        setImage(image);
    }

    @Override
    protected ButtonBase getButtonWidget() {
        return button;
    }

    public void setImage(ImageResource image) {
        button.setImage(image);
    }

    public void setCustomContentStyle(String customStyle) {
        button.setCustomContentStyle(customStyle);
    }

    @Override
    public int getTabIndex() {
        return getButtonWidget().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        getButtonWidget().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        getButtonWidget().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        getButtonWidget().setTabIndex(index);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public void setEnabled(boolean enabled) {
        getButtonWidget().setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return getButtonWidget().isEnabled();
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
}
