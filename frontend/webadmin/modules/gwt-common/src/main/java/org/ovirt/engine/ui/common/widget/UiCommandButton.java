package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    public UiCommandButton(IconType iconType) {
        this("", iconType); //$NON-NLS-1$
    }

    public UiCommandButton(String label, IconType iconType) {
        this();
        setLabel(label);
        getButtonWidget().setIcon(iconType);
    }

    @Override
    protected Button getButtonWidget() {
        return button;
    }

    public void setIcon(IconType iconType) {
        button.setIcon(iconType);
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
        tooltip.setHtml(buildTooltipHtml());
    }

    public void setAsPrimary() {
        button.setAsPrimary();
    }

    /**
     * Use prohibition reasons for tooltip
     */
    protected SafeHtml buildTooltipHtml() {
        SafeHtmlBuilder tooltipText = new SafeHtmlBuilder();
        if (!getCommand().getExecuteProhibitionReasons().isEmpty()) {
            for (String reason: getCommand().getExecuteProhibitionReasons()) {
                if (tooltipText.toSafeHtml().asString().length() != 0) {
                    tooltipText.appendHtmlConstant("<br/><br/>"); //$NON-NLS-1$
                }
                tooltipText.appendEscaped(reason);
            }
        }
        return tooltipText.toSafeHtml();
    }
}
