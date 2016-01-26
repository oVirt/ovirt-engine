package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractHeaderView;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractHeaderView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @WithElementId
    Anchor configureLink;

    @UiField
    @WithElementId
    Anchor feedbackLink;

    @UiField
    SimplePanel searchPanelContainer;

    @UiField
    SimplePanel mainTabContainer;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HeaderView(ApplicationDynamicMessages dynamicMessages) {
        this.configureLink = new Anchor(constants.configureLinkLabel());
        this.logoutLink = new Anchor(constants.logoutLinkLabel());
        this.optionsLink = new Anchor(constants.optionsLinkLabel());
        this.aboutLink = new Anchor(constants.aboutLinkLabel());
        this.guideLink = new Anchor(dynamicMessages.guideLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        this.logoLink.setHref(FrontendUrlUtils.getWelcomePageLink(GWT.getModuleBaseURL()));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == HeaderPresenterWidget.TYPE_SetSearchPanel) {
            setPanelContent(searchPanelContainer, content);
        } else if (slot == HeaderPresenterWidget.TYPE_SetTabBar) {
            setPanelContent(mainTabContainer, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public HasClickHandlers getConfigureLink() {
        return configureLink;
    }

    @Override
    public HasClickHandlers getFeedbackLink() {
        return feedbackLink;
    }

    @Override
    public void setFeedbackText(String feedbackText, String feedbackTitle) {
        feedbackLink.setText(feedbackText);
        if (feedbackTitle != null) {
            feedbackLink.setTitle(feedbackTitle);
        }
    }

}
