package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipConfig.Width;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WidgetWithInfo extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, WidgetWithInfo> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    Widget contentWidget;

    @UiField(provided = true)
    InfoIcon infoIcon;

    @Inject
    public WidgetWithInfo(Widget contentWidget) {
        this.contentWidget = contentWidget;
        infoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setExplanation(SafeHtml text) {
        infoIcon.setText(text);
    }

    public void addInfoIconStyle(String style) {
        infoIcon.addStyleName(style);
    }

    public void setInfoIconTooltipMaxWidth(Width width) {
        infoIcon.setTooltipMaxWidth(width);
    }

}
