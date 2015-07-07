package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.action.AbstractActionButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class MainTabBasicListItemActionButton extends AbstractActionButton implements HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, MainTabBasicListItemActionButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String buttonStyle();
    }

    @UiField
    Style style;

    @UiField
    Panel stylableButtonHolder;

    public MainTabBasicListItemActionButton(SafeHtml tooltip, ImageResource enabledImage, ImageResource disabledImage,
            String additionalStyle) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setTooltip(tooltip);
        setEnabledHtml(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML()));
        setDisabledHtml(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML()));
        stylableButtonHolder.addStyleName(additionalStyle);
    }

    @Override
    public void setElementId(String elementId) {
        button.getElement().setId(elementId);
    }

}
