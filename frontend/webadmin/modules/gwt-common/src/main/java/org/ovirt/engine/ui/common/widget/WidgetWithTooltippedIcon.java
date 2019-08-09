package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.TooltippedIcon;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public abstract class WidgetWithTooltippedIcon extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, WidgetWithTooltippedIcon> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    protected Widget contentWidget;

    @UiField(provided = true)
    protected TooltippedIcon icon;

    protected WidgetWithTooltippedIcon(Widget contentWidget, TooltippedIcon icon) {
        this.contentWidget = contentWidget;
        this.icon = icon;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setIconVisible(boolean visible) {
        icon.setVisible(visible);
    }

    public void setIconTooltipText(String text) {
        setIconTooltipText(SafeHtmlUtils.fromString(text));
    }

    public void setIconTooltipText(SafeHtml text) {
        icon.setText(text);
    }

    public void setIconTooltipMaxWidth(TooltipWidth width) {
        icon.setTooltipMaxWidth(width);
    }

    public void addIconStyle(String style) {
        icon.addStyleName(style);
    }
}
