package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.label.LabelWithToolTip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String mainTabBar();
    }

    @UiField
    Style style;

    @UiField
    @WithElementId("userName")
    InlineLabel userNameLabel;

    @UiField
    @Ignore
    Label headerLabel;

    @UiField(provided = true)
    @WithElementId
    final Anchor logoutLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor configureLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor aboutLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor guideLink;

    @UiField
    SimplePanel searchPanelContainer;

    @UiField
    SimplePanel mainTabContainer;

    @UiField
    HTMLPanel feedbackImagePanel;

    @UiField
    LabelWithToolTip feedbackImageLabel;

    @Inject
    public HeaderView(ApplicationConstants constants,
            ApplicationDynamicMessages dynamicMessages) {
        this.configureLink = new Anchor(constants.configureLinkLabel());
        this.logoutLink = new Anchor(constants.logoutLinkLabel());
        this.aboutLink = new Anchor(constants.aboutLinkLabel());
        this.guideLink = new Anchor(dynamicMessages.guideLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        localize(dynamicMessages);
    }

    private void localize(ApplicationDynamicMessages dynamicMessages) {
        headerLabel.setText(dynamicMessages.mainHeaderLabel());
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == HeaderPresenterWidget.TYPE_SetSearchPanel) {
            setPanelContent(searchPanelContainer, content);
        } else if (slot == HeaderPresenterWidget.TYPE_SetTabBar) {
            setPanelContent(mainTabContainer, content);
            content.asWidget().addStyleName(style.mainTabBar());
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setUserName(String userName) {
        userNameLabel.setText(userName);
    }

    @Override
    public HasClickHandlers getLogoutLink() {
        return logoutLink;
    }

    @Override
    public HasClickHandlers getAboutLink() {
        return aboutLink;
    }

    @Override
    public HasClickHandlers getGuideLink() {
        return guideLink;
    }

    @Override
    public HasClickHandlers getConfigureLink() {
        return configureLink;
    }

    @Override
    public HasClickHandlers getFeedbackLink() {
        return feedbackImageLabel;
    }

    @Override
    public void setFeedbackText(String feedbackText, String feedbackTitle) {
        feedbackImagePanel.setVisible(true);
        feedbackImageLabel.setText(feedbackText);
        if (feedbackTitle != null) {
            feedbackImageLabel.setTitle(feedbackTitle);
        }
    }

}
