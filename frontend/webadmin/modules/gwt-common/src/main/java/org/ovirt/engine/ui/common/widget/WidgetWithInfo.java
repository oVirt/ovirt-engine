package org.ovirt.engine.ui.common.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;

public class WidgetWithInfo extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, WidgetWithInfo> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    @UiField(provided = true)
    Widget contentWidget;

    @UiField(provided = true)
    InfoIcon infoIcon;

    @Inject
    public WidgetWithInfo(Widget contentWidget) {
        this.contentWidget = contentWidget;
        infoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML, resources);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setExplanation(SafeHtml text) {
        infoIcon.setText(text);
    }

    public void addInfoIconStyle(String style) {
        infoIcon.addStyleName(style);
    }

}
