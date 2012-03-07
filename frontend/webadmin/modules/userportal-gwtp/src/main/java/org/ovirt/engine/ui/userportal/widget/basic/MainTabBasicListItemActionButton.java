package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.common.widget.action.AbstractActionButton;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class MainTabBasicListItemActionButton extends AbstractActionButton {

    interface Style extends CssResource {
        String buttonStyle();
    }

    interface WidgetUiBinder extends UiBinder<Widget, MainTabBasicListItemActionButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    public MainTabBasicListItemActionButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void initialize(UserPortalImageButtonDefinition<UserPortalItemModel> buttonDefinition) {
        button.getUpFace().setHTML(buttonDefinition.getEnabledHtml());
        button.getUpDisabledFace().setHTML(buttonDefinition.getDisabledHtml());
        button.setTitle(buttonDefinition.getTitle());
    }

}
