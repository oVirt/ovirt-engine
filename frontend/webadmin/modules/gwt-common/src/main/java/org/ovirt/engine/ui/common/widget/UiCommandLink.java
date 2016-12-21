package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
        tooltip.setHtml(buildTooltipHtml());
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

    @Override
    protected Widget getButtonWidget() {
        return anchor;
    }
}
