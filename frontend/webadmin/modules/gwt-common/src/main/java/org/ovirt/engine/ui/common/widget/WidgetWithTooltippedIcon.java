package org.ovirt.engine.ui.common.widget;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.TooltippedIcon;
import org.ovirt.engine.ui.common.widget.dialog.WarnIcon;
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

    @UiField
    protected InfoIcon infoIcon;

    @UiField
    protected WarnIcon warnIcon;

    protected Map<Class<? extends TooltippedIcon>, TooltippedIcon> iconTypeToInstance = new HashMap<>();

    protected Class<? extends TooltippedIcon> displayedIconType;

    protected WidgetWithTooltippedIcon(Widget contentWidget, Class<? extends TooltippedIcon> iconType, SafeHtml iconTooltipText) {
        this.contentWidget = contentWidget;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        iconTypeToInstance.put(InfoIcon.class, infoIcon);
        iconTypeToInstance.put(WarnIcon.class, warnIcon);

        setDisplayedIconType(iconType);
        setIconVisible(true);
        setIconTooltipText(iconTooltipText);
    }

    public void setDisplayedIconType(Class<? extends TooltippedIcon> iconType) {
        if (iconType == null) {
            throw new IllegalArgumentException("Icon type cannot be null"); //$NON-NLS-1$
        }
        this.displayedIconType = iconType;
    }

    public void setIconVisible(boolean visible) {
        iconTypeToInstance.values().forEach(icon -> icon.setVisible(false));
        iconTypeToInstance.get(displayedIconType).setVisible(visible);
    }

    public void setIconTooltipText(String text) {
        setIconTooltipText(SafeHtmlUtils.fromString(text));
    }

    public void setIconTooltipText(SafeHtml text) {
        iconTypeToInstance.values().forEach(icon -> icon.setText(text));
    }

    public void setIconTooltipMaxWidth(TooltipWidth width) {
        iconTypeToInstance.values().forEach(icon -> icon.setTooltipMaxWidth(width));
    }

    public void addIconStyle(String style) {
        iconTypeToInstance.values().forEach(icon -> icon.addStyleName(style));
    }
}
