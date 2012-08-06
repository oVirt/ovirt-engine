package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.action.AbstractActionButton;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    public MainTabBasicListItemActionButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void initialize(UserPortalImageButtonDefinition<UserPortalItemModel> buttonDefinition, String additionalStyle) {
        initialize(buttonDefinition);
        stylableButtonHolder.addStyleName(additionalStyle);
    }

    public void initialize(UserPortalImageButtonDefinition<UserPortalItemModel> buttonDefinition) {
        button.getUpFace().setHTML(buttonDefinition.getEnabledHtml());
        button.getUpDisabledFace().setHTML(buttonDefinition.getDisabledHtml());
        button.setTitle(buttonDefinition.getTitle());
    }

    @Override
    public void setElementId(String elementId) {
        button.getElement().setId(elementId);
    }

}
